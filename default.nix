with import <nixpkgs> {};

stdenv.mkDerivation {
  name = "inkuire";

  buildInputs = with pkgs; [
    sbt
  ];
}
