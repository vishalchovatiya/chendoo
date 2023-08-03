import mill._, mill.scalalib._, mill.scalalib.publish._
import scalafmt._
import $ivy.`com.goyeau::mill-scalafix::0.3.1`, com.goyeau.mill.scalafix.ScalafixModule

object versions {
	val scala           = "2.13.10"
	val chisel3         = "3.6.0"
	val chiseltest      = "0.6.0"
	val scalatest       = "3.2.16"
	val semanticdb      = "4.5.13"
	val mainargs        = "0.5.0"
}

trait BaseProject extends ScalaModule with PublishModule {
	def scalaVersion   = versions.scala
	def publishVersion = "0.1.0"
	def projectName    = "chendoo"
	def pomSettings = PomSettings(
		description    = "RISC-V core written in Chisel",
		organization   = "com.chendoo",
		url            = "https://github.com/vishalchovatiya/chendoo",
		licenses       = Seq(License.MIT),
		versionControl = VersionControl.github("vishalchovatiya", "chendoo"),
		developers = Seq(
			Developer("Vishal Chovatiya", "Vishal Chovatiya", "https://github.com/vishalchovatiya"),
		),
	)

	def ivyDeps = super.ivyDeps() ++ Agg(
		// ivy"com.carlosedp::riscvassembler:${versions.riscvassembler}",
		ivy"com.lihaoyi::mainargs:${versions.mainargs}"
	)
}

trait HasChisel3 extends ScalaModule {
	def ivyDeps = super.ivyDeps() ++ Agg(
		ivy"edu.berkeley.cs::chisel3:${versions.chisel3}",
	)
	override def scalacPluginIvyDeps = super.scalacPluginIvyDeps() ++ Agg(
		ivy"edu.berkeley.cs:::chisel3-plugin:${versions.chisel3}",
	)
	// TODO: Run in parallel
	object test extends Tests with TestModule.ScalaTest {
		def ivyDeps = super.ivyDeps() ++ Agg(
		ivy"org.scalatest::scalatest:${versions.scalatest}",
		ivy"edu.berkeley.cs::chiseltest:${versions.chiseltest}",
		)
	}
}

trait CodeQuality extends ScalafixModule with ScalafmtModule {
	override def scalacPluginIvyDeps = super.scalacPluginIvyDeps() ++ Agg(
		ivy"org.scalameta:::semanticdb-scalac:${versions.semanticdb}",
	)
}

trait ScalacOptions extends ScalaModule {
	override def scalacOptions = T {
		super.scalacOptions() ++ Seq(
		"-unchecked",
		"-deprecation",
		"-language:reflectiveCalls",
		"-feature",
		"-Xcheckinit",
		"-Xfatal-warnings",
		"-Ywarn-dead-code",
		"-Ywarn-unused",
		"-Ymacro-annotations",
		)
	}
}

object chendoo extends BaseProject with HasChisel3 with ScalacOptions with CodeQuality {
	def mainClass = Some("chendoo.Top")
}

def runTasks(
	t: Seq[String],
)(
	implicit ev: eval.Evaluator,
) = T.task {
	mill.main.MainModule.evaluateTasks(
    ev,
    t.flatMap(x => x +: Seq("+")).flatMap(x => x.split(" ")).dropRight(1),
    mill.define.SelectMode.Separated,
	)(identity)
}

def lint(
	implicit ev: eval.Evaluator,
) = T.command {
	runTasks(Seq("__.fix", "mill.scalalib.scalafmt.ScalafmtModule/reformatAll __.sources"))
}