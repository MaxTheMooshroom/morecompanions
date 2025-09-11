{
  description = "Forge 1.18.2 Mod Development Flake";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-24.05";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };
      in
      {
        devShells.default = pkgs.mkShell {
          packages = with pkgs; [
            openjdk17
            gradle
            maven
            git
            packwiz
            xvfb-run
            mesa
            libGL
          ];

          JAVA_HOME = pkgs.openjdk17;
          GRADLE_OPTS = "-Dorg.gradle.jvmargs=-Xmx4G";
        };
      });
}
