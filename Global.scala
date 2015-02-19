import java.io.File

import com.typesafe.config.{Config, ConfigFactory}
import play.api._

object Global extends GlobalSettings {

	override def onStart(app: Application) {
		actors.ClusterDemo.start()
	}

	override def onStop(app: Application)  {
		actors.ClusterDemo.stop()
	}

	override def onLoadConfig(config: Configuration, path: File, classLoader: ClassLoader, mode: Mode.Mode): Configuration = {
		/**
		 * When the Play application is loaded, depending on which "mode" is set in application.conf (dev, test, or prod),
		 * that specific configuration file is loaded by this overridden method.
		 */

		val seed = config.getBoolean("seed").getOrElse(false)
		val akkaPort = config.getInt("akka.port").getOrElse(2501)

		val finalConfig = config ++ Configuration(remoteConfig("127.0.0.1", akkaPort))

		//val modeSpecificConfig = config ++ Configuration(ConfigFactory.load(s"application.${mode.toString.toLowerCase}.conf"))
		super.onLoadConfig(finalConfig, path, classLoader, mode)
	}

	def remoteConfig(hostname: String, port: Int): Config = {
		val configStr = s"""
      akka.remote.netty.tcp.hostname = $hostname
      akka.remote.netty.tcp.port = $port
    """
		//ConfigFactory.parseString(configStr).withFallback(commonConfig)
		ConfigFactory.parseString(configStr)
	}
}