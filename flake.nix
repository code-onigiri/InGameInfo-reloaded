{
  description = "InGameInfoXML multi-version Minecraft mod development environment";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
  };

  outputs = { self, nixpkgs }:
    let
      systems = [ "x86_64-linux" "aarch64-linux" ];
      forAllSystems = nixpkgs.lib.genAttrs systems;
    in
    {
      devShells = forAllSystems (system:
        let
          pkgs = import nixpkgs {
            inherit system;
            config.allowUnfree = true;
          };

          # Native libraries LWJGL/GLFW need when running the client. The dev
          # shell keeps the host GPU enabled; scripts/render-test.sh selects
          # Mesa's llvmpipe itself for reproducible headless captures.
          runtimeLibs = with pkgs; [
            alsa-lib
            dbus
            fontconfig
            freetype
            glib
            gtk3
            libGL
            libX11
            libXcursor
            libXext
            libXi
            libXinerama
            libXrandr
            libXrender
            libXtst
            libXxf86vm
            libdrm
            libgbm
            libpulseaudio
            mesa
            nss
            nspr
            udev
          ];

          commonTools = with pkgs; [
            git
            jq
            unzip
            zip
            imagemagick
            xdotool
            xvfb-run
            mesa
            libGL
          ];

          mkDevShell = jdk: pkgs.mkShell {
            packages = commonTools ++ [ jdk ];

            JAVA_HOME = jdk.home;
            LD_LIBRARY_PATH = pkgs.lib.makeLibraryPath runtimeLibs;

            shellHook = ''
              echo "InGameInfoXML dev shell"
              echo "  Java:   $(java -version 2>&1 | head -n 1)"
              echo "  javac:  $(javac -version 2>&1)"
              echo ""
              echo "  default / mc1201: 1.20.1, Forge 47.4.28, Java 17"
              echo "  mc1211:           1.21.1, NeoForge 21.1.264, Java 21"
              echo "  mc2612:           26.1.2, NeoForge 26.1.2.122, Java 25"
              echo ""
              echo "  build:  cd versions/1.20.1 && ./gradlew build"
              echo "  client: cd versions/1.20.1 && ./gradlew runClient  (host GPU)"
              echo "  headless HUD test: scripts/render-test.sh  (llvmpipe)"
            '';
          };
        in
        {
          default = mkDevShell pkgs.jdk17;
          mc1201 = mkDevShell pkgs.jdk17;
          mc1211 = mkDevShell pkgs.jdk21;
          mc2612 = mkDevShell pkgs.jdk25;
        });

      formatter = forAllSystems (system:
        let pkgs = import nixpkgs { inherit system; };
        in pkgs.nixpkgs-fmt);
    };
}
