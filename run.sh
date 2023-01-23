(javac --module-path $FX_PATH --add-modules javafx.controls Rendering3D.java || exit 1) && java --module-path $FX_PATH --add-modules javafx.controls Rendering3D $@
