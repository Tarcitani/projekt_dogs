<#
Gera as texturas originais do modelo 3D da Bola de Fogo Real (64x32 RGBA).
O núcleo é magma amarelo/laranja; a crosta tem ilhas vulcânicas e fissuras transparentes.
A malha e os anéis são procedurais em RoyalFireballRenderer. Nenhum asset do Ghast é usado.
Execute da raiz: powershell -File tools/gen_royal_fireball.ps1
#>
Add-Type -AssemblyName System.Drawing
$targetDir = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../src/main/resources/assets/solomod/textures/entity'))
$core = [System.Drawing.Bitmap]::new(64, 32)
$crust = [System.Drawing.Bitmap]::new(64, 32)
try {
    for ($y = 0; $y -lt 32; $y++) {
        for ($x = 0; $x -lt 64; $x++) {
            # Termos periódicos deixam a emenda da esfera contínua; não há aleatoriedade de execução.
            $a = $x * [Math]::PI * 2 / 64
            $wave = [Math]::Sin($a * 5 + $y * 0.55) + [Math]::Cos($a * 3 - $y * 0.7) + [Math]::Sin($a * 7 + $y * 0.22) * 0.5
            $heat = [Math]::Min(1, [Math]::Abs($wave) * 0.55)
            $core.SetPixel($x, $y, [Drawing.Color]::FromArgb(255, 255, [int](160 + $heat * 85), [int](25 + $heat * 125)))
            $n = ($x * 37 + $y * 17) % 19
            if ([Math]::Abs($wave) -lt 0.33) {
                $color = [Drawing.Color]::FromArgb(0, 255, 170, 30)
            } elseif ([Math]::Abs($wave) -lt 0.65) {
                $color = [Drawing.Color]::FromArgb(255, 255, 80 + $n * 3, 12)
            } else {
                $color = [Drawing.Color]::FromArgb(255, 45 + $n, 16 + [int]($n / 2), 20 + [int]($n / 3))
            }
            $crust.SetPixel($x, $y, $color)
        }
    }
    $core.Save((Join-Path $targetDir 'royal_fireball_core.png'), [Drawing.Imaging.ImageFormat]::Png)
    $crust.Save((Join-Path $targetDir 'royal_fireball.png'), [Drawing.Imaging.ImageFormat]::Png)
    Write-Output 'Geradas: royal_fireball_core.png e royal_fireball.png (64x32 RGBA)'
} finally {
    $core.Dispose()
    $crust.Dispose()
}
