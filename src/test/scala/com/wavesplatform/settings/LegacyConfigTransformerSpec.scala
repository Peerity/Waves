package com.wavesplatform.settings

import java.io.File
import java.net.InetSocketAddress
import java.time.Duration

import com.typesafe.config.ConfigFactory
import com.wavesplatform.state2.ByteStr
import org.scalatest.{FreeSpec, Matchers}

import scala.concurrent.duration._

class LegacyConfigTransformerSpec extends FreeSpec with Matchers {
  private val legacyConfig =
    """{
      |  "p2p": {
      |    "localOnly": true,
      |    "myAddress": "1.2.3.4:6868",
      |    "nodeName": "test-node-name",
      |    "bindAddress": "1.2.3.4",
      |    "port": 6886,
      |    "upnp": false,
      |    "upnpGatewayTimeout": 7000,
      |    "upnpDiscoverTimeout": 3000,
      |    "knownPeers": [
      |      "138.201.152.166:6868",
      |      "138.201.152.165:6868"
      |    ],
      |    "maxConnections": 10,
      |    "peersDataResidenceTimeDays": 2,
      |    "blacklistResidenceTimeMilliseconds": 45000,
      |    "connectionTimeout": 5,
      |    "outboundBufferSizeMb": 2,
      |    "minEphemeralPortNumber": 30000,
      |    "maxUnverifiedPeers": 200,
      |    "peersDataBroadcastDelay": 15000,
      |    "upnpGatewayTimeout": 100,
      |    "upnpDiscoverTimeout": 100
      |  },
      |  "loadEntireChain": false,
      |  "maxChain": 13,
      |  "utxSize": 255,
      |  "historySynchronizerTimeout": 30,
      |  "pinToInitialPeer": true,
      |  "retriesBeforeBlacklisted": 10,
      |  "utxRebroadcastInterval": "5",
      |  "blacklistThreshold": 10,
      |  "operationRetries": 88,
      |  "scoreBroadcastDelay": 15000,
      |  "walletDir": "/root/waves/wallet",
      |  "walletSeed": "WALLETSEED",
      |  "walletPassword": "ridethewaves!",
      |  "dataDir": "/root/waves/data",
      |  "rpcEnabled": false,
      |  "rpcPort": 8888,
      |  "rpcAddress": "1.3.5.7",
      |  "blockGenerationDelay": 15000,
      |  "historySynchronizerTimeout": 15,
      |  "cors": false,
      |  "maxRollback": 90,
      |  "apiKeyHash": "H6nsiifwYKYEx6YzYD7woP1XCn72RVvx6tC1zjjLX",
      |  "history": "blockchain",
      |  "offlineGeneration": false,
      |  "testnet": false,
      |  "loggingLevel": "info",
      |  "genesisSignature": "FSH8eAAzZNqnG8xgTZtz5xuLqXySsXgAjmFEC25hXMbEufiGjqWPnGCZFt6gLiVLJny16ipxRNAkkzjjhqTjBE2",
      |  "checkpoints": {
      |    "publicKey": "7EXnkmJyz1gPfLJwytThcwGwpyfjzFXC3hxBhvVK4"
      |  },
      |  "minerEnabled": false,
      |  "offlineGeneration": true,
      |  "blockGenerationDelay": 5000,
      |  "quorum": 10,
      |  "tflikeScheduling": false,
      |  "allowedGenerationTimeFromLastBlockInterval": 2,
      |  "logLevel": "warn",
      |  "feeMap": {
      |    "2": { "Waves": 100000 },
      |    "3": { "Waves": 100000000 },
      |    "4": { "Waves": 100000, "4764Pr9DpKQAHAjAVA2uqnrYidLMnM7vpDDLCDWujFTt": 1 },
      |    "5": { "Waves": 100000 },
      |    "6": { "Waves": 100000 },
      |    "7": { "Waves": 100000 },
      |    "8": { "Waves": 100000 }
      |    "9": { "Waves": 100000 }
      |  }
      |}
      |""".stripMargin

  private val defaultLegacyConfig =
    """{
      |  "p2p": {
      |    "localOnly": true,
      |    "myAddress": "1.2.3.4:6868",
      |    "nodeName": "test-node-name",
      |    "bindAddress": "1.2.3.4",
      |    "port": 6886,
      |    "upnp": false,
      |    "upnpGatewayTimeout": 7000,
      |    "upnpDiscoverTimeout": 3000,
      |    "knownPeers": [
      |      "138.201.152.166:6868",
      |      "138.201.152.165:6868"
      |    ],
      |    "maxConnections": 10,
      |    "peersDataResidenceTimeDays": 2,
      |    "blacklistResidenceTimeMilliseconds": 45000,
      |    "connectionTimeout": 5,
      |    "outboundBufferSizeMb": 2,
      |    "minEphemeralPortNumber": 30000,
      |    "maxUnverifiedPeers": 200,
      |    "peersDataBroadcastDelay": 15000,
      |    "upnpGatewayTimeout": 100,
      |    "upnpDiscoverTimeout": 100
      |  },
      |  "loadEntireChain": false,
      |  "maxChain": 13,
      |  "utxSize": 255,
      |  "historySynchronizerTimeout": 30,
      |  "pinToInitialPeer": true,
      |  "retriesBeforeBlacklisted": 10,
      |  "utxRebroadcastInterval": "5",
      |  "blacklistThreshold": 10,
      |  "operationRetries": 88,
      |  "scoreBroadcastDelay": 15000,
      |  "walletDir": "",
      |  "walletSeed": "WALLETSEED",
      |  "walletPassword": "ridethewaves!",
      |  "dataDir": "",
      |  "rpcEnabled": false,
      |  "rpcPort": 8888,
      |  "rpcAddress": "1.3.5.7",
      |  "blockGenerationDelay": 15000,
      |  "historySynchronizerTimeout": 15,
      |  "cors": false,
      |  "maxRollback": 90,
      |  "apiKeyHash": "H6nsiifwYKYEx6YzYD7woP1XCn72RVvx6tC1zjjLX",
      |  "history": "blockchain",
      |  "offlineGeneration": false,
      |  "testnet": false,
      |  "loggingLevel": "info",
      |  "genesisSignature": "FSH8eAAzZNqnG8xgTZtz5xuLqXySsXgAjmFEC25hXMbEufiGjqWPnGCZFt6gLiVLJny16ipxRNAkkzjjhqTjBE2",
      |  "checkpoints": {
      |    "publicKey": "7EXnkmJyz1gPfLJwytThcwGwpyfjzFXC3hxBhvVK4"
      |  },
      |  "minerEnabled": false,
      |  "offlineGeneration": true,
      |  "blockGenerationDelay": 5000,
      |  "quorum": 10,
      |  "tflikeScheduling": false,
      |  "allowedGenerationTimeFromLastBlockInterval": 2,
      |  "logLevel": "warn",
      |}
      |""".stripMargin

  "properly parses default legacy config file" in {
    val legacyConfigFromJson = LegacyConfigTransformer
      .transform(ConfigFactory.parseString(defaultLegacyConfig))
      .withFallback(ConfigFactory.parseString(
        """waves {
          |  blockchain {
          |    blockchain-file = ""
          |    state-file = ""
          |    checkpoint-file = ""
          |    minimum-in-memory-diff-blocks = 200
          |  }
          |  network {
          |    file = ""
          |    unrequested-packets-threshold = 100
          |    max-inbound-connections = 30
          |    max-outbound-connections = 30
          |    max-single-host-connections = 3
          |    handshake-timeout = 30s
          |  }
          |  utx {
          |    max-size = 10000
          |    max-transaction-age = 90m
          |  }
          |  matcher {
          |    enable = false
          |    account = ""
          |    bind-address = ""
          |    port = 0
          |    min-order-fee = 0
          |    order-match-tx-fee = 0
          |    journal-directory = ""
          |    snapshots-directory = ""
          |    snapshots-interval = 10m
          |    max-open-orders = 1000
          |    price-assets = []
          |    predefined-pairs = []
          |    order-history-file = ""
          |    max-timestamp-diff = 3h
          |  }
          |  synchronization.score-ttl = 90s
          |}
          |""".stripMargin))

    val ws = WavesSettings.fromConfig(legacyConfigFromJson)

    ws.walletSettings.file shouldBe Some(new File(s"${System.getProperty("user.home")}/wallet/wallet.dat"))
    ws.directory shouldBe s"${System.getProperty("user.home")}"
  }

  "properly parses custom values from legacy config file" in {
    val legacyConfigFromJson = LegacyConfigTransformer
      .transform(ConfigFactory.parseString(legacyConfig))
      .withFallback(ConfigFactory.parseString(
        """waves {
          |  blockchain {
          |    blockchain-file = ""
          |    state-file = ""
          |    checkpoint-file = ""
          |    minimum-in-memory-diff-blocks = 200
          |  }
          |  network {
          |    file = ""
          |    unrequested-packets-threshold = 100
          |    max-single-host-connections = 3
          |    handshake-timeout = 30s
          |  }
          |  utx {
          |    max-size = 10000
          |    max-transaction-age = 90m
          |  }
          |  matcher {
          |    enable = false
          |    account = ""
          |    bind-address = ""
          |    port = 0
          |    min-order-fee = 0
          |    order-match-tx-fee = 0
          |    journal-directory = ""
          |    snapshots-directory = ""
          |    order-history-file = ""
          |    snapshots-interval = 10m
          |    max-open-orders = 1000
          |    price-assets = []
          |    predefined-pairs = []
          |    max-timestamp-diff = 3h
          |  }
          |  synchronization.score-ttl = 90s
          |}
          |""".stripMargin))

    val ws = WavesSettings.fromConfig(legacyConfigFromJson)
    ws.networkSettings should have (
      'declaredAddress (Some(new InetSocketAddress("1.2.3.4", 6868))),
      'nodeName ("test-node-name"),
      'bindAddress (new InetSocketAddress("1.2.3.4", 6886)),
      'knownPeers (Seq("138.201.152.166:6868", "138.201.152.165:6868")),
      'peersDataResidenceTime (2.days),
      'blackListResidenceTime (45.seconds),
      'maxInboundConnections (10),
      'maxOutboundConnections (10),
      'maxConnectionsPerHost (3),
      'connectionTimeout (5.seconds),
      'outboundBufferSize (2 * 1024 * 1024),
      'maxUnverifiedPeers (200),
      'peersBroadcastInterval (15.seconds)
    )

    ws.restAPISettings should have (
     'enable (false),
     'bindAddress ("1.3.5.7"),
     'port (8888),
     'apiKeyHash ("H6nsiifwYKYEx6YzYD7woP1XCn72RVvx6tC1zjjLX"),
     'cors (false)
    )

    ws.utxSettings should have (
      'maxSize (10000),
      'maxTransactionAge (90.minutes)
    )

    ws.checkpointsSettings should have (
      'publicKey (ByteStr.decodeBase58("7EXnkmJyz1gPfLJwytThcwGwpyfjzFXC3hxBhvVK4").get)
    )

    ws.loggingLevel shouldBe LogLevel.WARN
    ws.directory shouldBe "/root/waves"

    ws.walletSettings.file shouldBe Some(new File("/root/waves/wallet/wallet.s.dat"))

    ws.minerSettings should have (
      'enable (false),
      'quorum (10),
      'intervalAfterLastBlockThenGenerationIsAllowed (Duration.ofDays(2))
    )

    ws.synchronizationSettings should have (
      'maxRollback (90),
      'maxChainLength (13),
      'loadEntireChain (false),
      'synchronizationTimeout (15.seconds),
      'pinToInitialPeer (true),
      'retriesBeforeBlacklisting (10),
      'operationRetries (88),
      'scoreBroadcastInterval (15.seconds)
    )
  }
}
