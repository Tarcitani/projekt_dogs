# Como colaborar

Guia rápido para trabalharmos juntos no Solo Mod sem pisar no código um do outro. Setup do ambiente: veja [README.md](README.md#primeiros-passos-para-quem-vai-colaborar).

## Acesso

O dono do repositório adiciona o colaborador em **GitHub → Settings → Collaborators → Add people**. Depois de aceitar o convite, é só clonar:

```bash
git clone https://github.com/Tarcitani/projekt_dogs.git
```

## Fluxo de trabalho

A `main` deve sempre compilar e abrir o jogo. Nada de commit direto nela: cada tarefa vai numa branch.

```bash
git checkout main
git pull                                  # pega o que o outro já subiu
git checkout -b feat/nome-curto           # crie a branch da tarefa
# ... programe, teste com ./gradlew runClient ...
git add -A
git commit -m "feat: descreve o que mudou"
git push -u origin feat/nome-curto        # depois abra o Pull Request no GitHub
```

1. Abra um **Pull Request** para a `main` e peça para o outro revisar.
2. Antes de pedir revisão, rode `./gradlew build` e teste no jogo.
3. Depois do merge, apague a branch e volte para a `main` com `git pull`.

Se a `main` andou enquanto você trabalhava: `git pull origin main` na sua branch, resolva os conflitos, teste de novo e faça push.

### Nomes de branch e commits

| Prefixo | Uso | Exemplo |
|---|---|---|
| `feat/` | funcionalidade nova | `feat/arco-elemental` |
| `fix/` | correção de bug | `fix/mana-nao-regenera` |
| `art/` | texturas, modelos, scripts de `tools/` | `art/soberano-agua` |
| `docs/` | documentação | `docs/readme-comandos` |
| `refactor/` | reorganização sem mudar comportamento | `refactor/brains` |

Use o mesmo prefixo no começo da mensagem de commit (`feat: ...`, `fix: ...`).

## Dividindo o trabalho

Para evitar conflitos, combinem quem mexe em cada área (veja a tabela de pacotes no README). Os pontos de conflito mais comuns:

- **Registros centrais**: `SoloMod.java`, `ModItems`, `ModEntities`, `ModNetwork`, `ModAttachments`, `ModDataComponents`. Mudanças pequenas e PRs rápidos.
- **Arquivos de idioma**: `lang/pt_br.json` e `lang/en_us.json`. Adicione as chaves **nos dois** arquivos e mantenha a ordem alfabética.
- **Arquivos gerados**: nunca edite `client/model/MobMeshes.java` ou as texturas geradas à mão. Mude o script em `tools/`, rode-o e faça commit do script **e** do resultado juntos. Se der conflito num arquivo gerado, aceite qualquer lado e **rode o script de novo**.

## Convenções de código

- Java 21, encoding UTF-8, indentação de 4 espaços.
- Nomes: o código mistura inglês e português (ex.: `addXp`, `getPontosDisponiveis`). Siga o padrão do arquivo que estiver editando. Comentários em português.
- Textos novos visíveis no jogo: prefira `Component.translatable(...)` com a chave em `lang/`. Ainda há textos fixos com `Component.literal` (ex.: comandos de teste), que podem ser migrados aos poucos.
- Lógica de jogo fica no servidor (`event/`, `mob/`, `skill/`). O cliente (`client/`) só desenha e recebe pacotes de `network/`. Nunca importe classes de `client/` em código comum; use `ClientAccess`.
- Ao criar um pacote de rede novo, registre-o em `ModNetwork`.
- Ao criar um comando de teste novo, documente-o no Javadoc de `ModTestCommand` **e** na tabela do README.

## O que não vai para o Git

Já está no `.gitignore`: `build/`, `.gradle/`, `run/` (mundo e configs locais), `bin/`, `.vscode/launch.json`, `__pycache__/` e arquivos `javac.*.args`. Se aparecer algo gerado no `git status`, adicione ao `.gitignore` em vez de commitar.

## Checklist do Pull Request

- [ ] `./gradlew build` passa
- [ ] Testado no jogo (`./gradlew runClient`)
- [ ] Textos novos estão em `pt_br.json` e `en_us.json`
- [ ] Arquivos gerados foram regerados pelo script (se aplicável)
- [ ] README atualizado se mudou comando, mecânica ou estrutura
