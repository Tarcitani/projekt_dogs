<# Compatibilidade: a fonte dos rostos fica em gen_boss_icons.py. #>
python (Join-Path $PSScriptRoot 'gen_boss_icons.py')
if ($LASTEXITCODE -ne 0) { throw 'Falha ao gerar os icones dos chefes.' }
