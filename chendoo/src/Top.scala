package chendoo

// import chisel3._
import circt.stage.ChiselStage
import mainargs.{Leftover, ParserForMethods, arg, main}

// The Main object extending App to generate the Verilog code.
object Top {
  @main
  def run(
      // Parse command line arguments and extract required parameters
      @arg(short = 'c', doc = "Chisel arguments") chiselArgs: Leftover[String],
    ): Unit =
    // Generate Verilog
    ChiselStage.emitSystemVerilogFile(
      new GCD(),
      chiselArgs.value.toArray,
      Array(
        "--disable-all-randomization",
        "--strip-debug-info",
        "-lower-memories",
        "--lowering-options=disallowLocalVariables,disallowPackedArrays", // Ref. https://github.com/llvm/circt/issues/4751
      ),
    )

  def main(
      args: Array[String],
    ): Unit = ParserForMethods(this).runOrExit(args.toIndexedSeq)
}
