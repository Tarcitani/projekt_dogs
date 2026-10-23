<#
Gera o ícone de conquista do Rei Vulcânico em pixel art RGBA, sem suavização.
Cabeça de carvão, olhos de brasa, mandíbula e coroa derretida com três chamas,
seguindo head/jaw/crown de mob_variants.monarch_fire.
Execute da raiz no Windows: powershell -File tools/gen_boss_icons.ps1
#>
Add-Type -AssemblyName System.Drawing
$palette = @{
    '.' = '00000000'; 'x' = 'FF181118'; 'c' = 'FF34282B'; 's' = 'FF573D36'
    'b' = 'FF86461D'; 'g' = 'FFE3972C'; 'h' = 'FFFFD561'; 'r' = 'FFDC3012'
    'f' = 'FFFF6F14'; 'y' = 'FFFFEB8E'
}
# Matriz explícita: pixels revisáveis e resultado determinístico.
$pixels = @(
    '................'
    '.......r........'
    '...r...fr...r...'
    '..rf..ryfr..fr..'
    '..fy..fyyf..yf..'
    '..gh..ghhg..hg..'
    '.bghggghhggghgb.'
    '.bggggggggggggb.'
    '..xxxxxxxxxxxx..'
    '..xsscccccccsx..'
    '..xsyfxccxfysx..'
    '..xccrrccrrccx..'
    '..xcccxssxcccx..'
    '..xsxrrrrrrxsx..'
    '...xsfyffyfsx...'
    '....xxxxxxxx....'
)
$bitmap = [System.Drawing.Bitmap]::new(16, 16)
try {
    for ($y = 0; $y -lt 16; $y++) {
        if ($pixels[$y].Length -ne 16) { throw "Linha $y deve ter 16 pixels" }
        for ($x = 0; $x -lt 16; $x++) {
            $argb = [Convert]::ToInt32($palette[[string]$pixels[$y][$x]], 16)
            $bitmap.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($argb))
        }
    }
    $target = Join-Path $PSScriptRoot '../src/main/resources/assets/solomod/textures/item/monarch_fire_head.png'
    $bitmap.Save([IO.Path]::GetFullPath($target), [System.Drawing.Imaging.ImageFormat]::Png)
    Write-Output 'Gerado: textures/item/monarch_fire_head.png (16x16 RGBA)'
} finally {
    $bitmap.Dispose()
}
