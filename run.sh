(javac --module-path $FX_PATH --add-modules javafx.controls $1.java || exit 1) && java -Dprism.forceGPU --module-path $FX_PATH --add-modules javafx.controls $1 ${@:2}
