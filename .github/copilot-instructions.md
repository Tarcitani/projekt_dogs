# Instruções para o GitHub Copilot

As instruções para agentes de IA deste repositório ficam em [AGENTS.md](../AGENTS.md), na raiz. Siga as regras de lá. Em especial:

- Mod NeoForge 1.21.1, Java 21, pacote `com.tarcitani.solomod`. Comentários e textos em português.
- Não edite arquivos gerados (`client/model/MobMeshes.java`, texturas de `textures/entity/`): altere os scripts de `tools/`.
- Nada fora de `client/` pode importar classes de cliente.
- Não reordene enums de `core/` (ordinais são usados na rede e nos modelos de item).
- Textos novos vão em `lang/pt_br.json` e `lang/en_us.json`.

Mapa de arquivos: [docs/ARCHITECTURE.md](../docs/ARCHITECTURE.md).
