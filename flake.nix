{
  description = "InGameInfoXML multi-version development environment";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
  };

  outputs = { self, nixpkgs }:
    let
      system = "x86_64-linux";
      pkgs = import nixpkgs { inherit system; };

      # Native libraries LWJGL / GLFW need when running the Minecraft client
      # under Xvfb with software rendering.
      runtimeLibs = with pkgs; [
        alsa-lib
        libglvnd
        mesa
        pulseaudio
        udev
        xorg.libX11
        xorg.libXcursor
        xorg.libXext
        xorg.libXi
        xorg.libXrandr
        xorg.libXxf86vm
      ];

      commonTools = with pkgs; [
        git
        gradle
        imagemagick
        xdotool
        xvfb-run
        jq
        unzip
        which
      ];

      mkDevShell = jdk: pkgs.mkShell {
        packages = commonTools ++ [ jdk ];

        JAVA_HOME = jdk.home;
        GRADLE_USER_HOME = "$PWD/.gradle";
        LD_LIBRARY_PATH = pkgs.lib.makeLibraryPath runtimeLibs;
        LIBGL_ALWAYS_SOFTWARE = "1";
        MESA_LOADER_DRIVER_OVERRIDE = "llvmpipe";
        __GLX_VENDOR_LIBRARY_NAME = "mesa";

        shellHook = ''
          echo "InGameInfoXML dev shell"
          echo "  Java:   $(java -version 2>&1 | head -n 1)"
          echo "  Gradle: $(gradle --version | sed -n '2p')"
          echo "  Version projects: versions/1.20.1 (JDK 17), versions/1.21.1 (JDK 21), versions/26.1.2 (JDK 25)"
          echo "  Headless client: xvfb-run -a ./gradlew runClient"
        '';
      };
    in {
      devShells.${system} = {
        default = mkDevShell pkgs.jdk17;
        jdk17 = mkDevShell pkgs.jdk17;
        jdk21 = mkDevShell pkgs.jdk21;
        jdk25 = mkDevShell pkgs.jdk25;
      };

      formatter.${system} = pkgs.nixpkgs-fmt;
    };
}
