<#
Gera as texturas originais da Bola de Fogo Real em 16x16 RGBA com pixels grandes, no estilo do Minecraft
(igual às esferas de água e aos tornados do Soberano). A malha é de CUBOS, em RoyalFireballRenderer:
  * royal_fireball.png       -> crosta de basalto com fissuras de lava (alguns pixels vazados)
  * royal_fireball_core.png  -> núcleo de magma (branco, amarelo e laranja)
O "ruído" é um hash de (x, y): a mesma saída sempre, sem aleatoriedade de execução.
Execute da raiz: powershell -File tools/gen_royal_fireball.ps1
#>
Add-Type -AssemblyName System.Drawing
$targetDir = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../src/main/resources/assets/solomod/textures/entity'))

function Get-Noise([int]$x, [int]$y, [int]$k) {
    return ((($x * 73856093) -bxor ($y * 19349663) -bxor ($k * 83492791)) % 1000) / 1000.0
}

$crust = [System.Drawing.Bitmap]::new(16, 16)
$core = [System.Drawing.Bitmap]::new(16, 16)
try {
    for ($y = 0; $y -lt 16; $y++) {
        for ($x = 0; $x -lt 16; $x++) {
            $r = Get-Noise $x $y 2
            if ($r -lt 0.12) {
                $color = [Drawing.Color]::FromArgb(0, 0, 0, 0)
            } elseif ($r -lt 0.5) {
                $color = [Drawing.Color]::FromArgb(255, 54, 24, 26)
            } elseif ($r -lt 0.8) {
                $color = [Drawing.Color]::FromArgb(255, 255, 112, 24)
            } else {
                $color = [Drawing.Color]::FromArgb(255, 255, 176, 48)
            }
            $crust.SetPixel($x, $y, $color)

            $r = Get-Noise $x $y 3
            if ($r -lt 0.35) {
                $color = [Drawing.Color]::FromArgb(255, 255, 255, 255)
            } elseif ($r -lt 0.75) {
                $color = [Drawing.Color]::FromArgb(255, 255, 236, 140)
            } else {
                $color = [Drawing.Color]::FromArgb(255, 255, 190, 60)
            }
            $core.SetPixel($x, $y, $color)
        }
    }
    $crust.Save((Join-Path $targetDir 'royal_fireball.png'), [Drawing.Imaging.ImageFormat]::Png)
    $core.Save((Join-Path $targetDir 'royal_fireball_core.png'), [Drawing.Imaging.ImageFormat]::Png)
    Write-Output 'Geradas: royal_fireball.png e royal_fireball_core.png (16x16 RGBA)'
} finally {
    $crust.Dispose()
    $core.Dispose()
}
