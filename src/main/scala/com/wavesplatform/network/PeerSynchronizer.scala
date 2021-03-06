package com.wavesplatform.network

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter}
import scorex.utils.ScorexLogging

import scala.concurrent.duration.FiniteDuration

@Sharable
class PeerSynchronizer(peerDatabase: PeerDatabase, peerRequestInterval: FiniteDuration) extends ChannelInboundHandlerAdapter
  with ScorexLogging {
  def requestPeers(ctx: ChannelHandlerContext): Unit = ctx.executor().schedule(peerRequestInterval) {
    if (ctx.channel().isActive) {
      log.trace(s"${id(ctx)} Requesting peers")
      ctx.writeAndFlush(GetPeers)
      requestPeers(ctx)
    }
  }

  override def channelActive(ctx: ChannelHandlerContext) = {
    requestPeers(ctx)
    super.channelActive(ctx)
  }

  override def channelRead(ctx: ChannelHandlerContext, msg: AnyRef) = msg match {
    case hs: Handshake =>
      hs.declaredAddress.foreach { declaredAddress =>
        peerDatabase.addCandidate(declaredAddress)
        ctx.channel().attr(AttributeKeys.DeclaredAddress).setIfAbsent(declaredAddress)
      }
      ctx.fireChannelRead(msg)
    case GetPeers =>
      ctx.channel().declaredAddress.foreach(peerDatabase.touch)
      ctx.writeAndFlush(KnownPeers(peerDatabase.knownPeers.keys.toSeq))
    case KnownPeers(peers) =>
      log.trace(s"${id(ctx)} Got known peers: ${peers.mkString("[", ", ", "]")}")
      ctx.channel().declaredAddress.foreach(peerDatabase.touch)
      peers.foreach(peerDatabase.addCandidate)
    case _ =>
      ctx.channel().declaredAddress.foreach(peerDatabase.touch)
      super.channelRead(ctx, msg)
  }
}
