package com.tarcitani.solomod.client;

import com.tarcitani.solomod.attachment.PlayerStatsAttachment;
import com.tarcitani.solomod.core.WeaponClass;
import com.tarcitani.solomod.event.StatsLogic;
import com.tarcitani.solomod.item.ModItems;
import com.tarcitani.solomod.network.UnlockTalentPayload;
import com.tarcitani.solomod.skill.SkillTree;
import com.tarcitani.solomod.skill.WeaponSkill;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;

/**
 * Árvore única em K → Habilidades: rosto no centro, quatro armas normais do catálogo em círculo e habilidades
 * ramificando como raízes. Não há abas/filtros por arma. Molduras e estados são das conquistas vanilla.
 * Arraste nos dois eixos, use roda para zoom e setas para navegar; centralizar restaura a visão geral.
 * Aprender apenas solicita a compra ao servidor, sem alterar progressão no cliente.
 */
public final class SkillTreeScreen extends Screen {
    private static final ResourceLocation STONE = ResourceLocation.withDefaultNamespace("textures/block/stone.png");
    private final Screen parent;
    private SkillTree.Node selected;
    private WeaponClass inspectedWeapon;
    private int left, top, panelW, panelH, mapX, mapY, mapW, mapH;
    private double panX, panY;
    private float zoom = 0.6F;
    private boolean initialized;
    private Button learn;
    private final List<MapButton> nodes = new ArrayList<>();

    public SkillTreeScreen(Screen parent) {
        super(Component.translatable("screen.solomod.tab_skills"));
        this.parent = parent;
    }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() { minecraft.setScreen(parent); }
    private PlayerStatsAttachment stats() { return StatsLogic.stats(minecraft.player); }

    @Override protected void init() {
        panelW = Math.min(760, width - 12); panelH = Math.min(620, height - 12);
        left = (width - panelW) / 2; top = (height - panelH) / 2;
        mapX = left + 8; mapY = top + 31; mapW = panelW - 16; mapH = panelH - 91;
        nodes.clear();
        addRenderableWidget(Button.builder(Component.translatable("gui.back"), b -> onClose())
                .bounds(left + panelW - 65, top + 7, 57, 18).build());
        addRenderableWidget(Button.builder(Component.literal("−"), b -> changeZoom(zoom / 1.2F, centerX(), centerY()))
                .bounds(mapX, mapY + mapH - 22, 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal("+"), b -> changeZoom(zoom * 1.2F, centerX(), centerY()))
                .bounds(mapX + 23, mapY + mapH - 22, 20, 20).build());
        var center = addRenderableWidget(Button.builder(Component.translatable("screen.solomod.tree_center"), b -> centerMap())
                .bounds(mapX + 46, mapY + mapH - 22, 72, 20).build());
        center.setTooltip(Tooltip.create(Component.translatable("screen.solomod.tree_navigation")));
        for (var weapon : SkillTreeLayout.WEAPONS) nodes.add(addRenderableWidget(new MapButton(weapon, null)));
        for (var node : SkillTreeLayout.NODES) nodes.add(addRenderableWidget(new MapButton(node.weapon(), node)));
        learn = addRenderableWidget(Button.builder(Component.translatable("screen.solomod.learn"), b -> {
            if (selected != null && SkillTree.available(stats(), selected)) PacketDistributor.sendToServer(new UnlockTalentPayload(selected.id()));
        }).bounds(left + panelW - 84, top + panelH - 49, 76, 20).build());
        if (!initialized) { centerMap(); initialized = true; } else updateNodes();
    }
    private int centerX() { return mapX + mapW / 2; }
    private int centerY() { return mapY + (mapH - 24) / 2; }
    private int screenX(SkillTreeLayout.Point p) { return (int) Math.round(centerX() + (p.x() + panX) * zoom); }
    private int screenY(SkillTreeLayout.Point p) { return (int) Math.round(centerY() + (p.y() + panY) * zoom); }
    private void centerMap() {
        panX = panY = 0;
        zoom = Mth.clamp(Math.min(mapW, mapH - 26) / 352F, 0.35F, 0.9F);
        updateNodes();
    }
    private void changeZoom(float target, double mouseX, double mouseY) {
        float next = Mth.clamp(target, 0.16F, 1.75F);
        double worldX = (mouseX - centerX()) / zoom - panX, worldY = (mouseY - centerY()) / zoom - panY;
        panX = (mouseX - centerX()) / next - worldX;
        panY = (mouseY - centerY()) / next - worldY;
        zoom = next;
        updateNodes();
    }
    private void updateNodes() {
        if (learn == null) return;
        panX = Mth.clamp(panX, -SkillTreeLayout.EXTENT, SkillTreeLayout.EXTENT);
        panY = Mth.clamp(panY, -SkillTreeLayout.EXTENT, SkillTreeLayout.EXTENT);
        for (var button : nodes) {
            int size = Math.max(4, Math.round(26 * zoom));
            button.setWidth(size); button.setHeight(size);
            button.setX(screenX(button.position()) - size / 2);
            button.setY(screenY(button.position()) - size / 2);
            button.visible = button.getX() + size > mapX && button.getX() < mapX + mapW
                    && button.getY() + size > mapY && button.getY() < mapY + mapH - 24;
        }
        learn.active = selected != null && SkillTree.available(stats(), selected);
        learn.setMessage(Component.translatable(selected != null && stats().hasTalent(selected.id()) ? "screen.solomod.learned" : "screen.solomod.learn"));
    }
    @Override public void tick() { if (minecraft.player != null) updateNodes(); }

    @Override public void renderBackground(GuiGraphics g, int mx, int my, float partial) {
        g.fill(0, 0, width, height, 0xB0101010);
        g.fill(left, top, left + panelW, top + panelH, 0xFF111111);
        g.fill(left + 1, top + 1, left + panelW - 1, top + panelH - 1, 0xFFEEEEEE);
        g.fill(left + 3, top + 3, left + panelW - 3, top + panelH - 3, 0xFF555555);
        g.fill(left + 4, top + 4, left + panelW - 4, top + panelH - 4, 0xFFC6C6C6);
        g.enableScissor(mapX, mapY, mapX + mapW, mapY + mapH);
        int offsetX = (int) (panX * zoom) % 16, offsetY = (int) (panY * zoom) % 16;
        for (int x = -16; x < mapW + 16; x += 16) for (int y = -16; y < mapH + 16; y += 16)
            g.blit(STONE, mapX + x + offsetX, mapY + y + offsetY, 0, 0, 16, 16, 16, 16);
        g.fill(mapX, mapY, mapX + mapW, mapY + mapH, 0xB8000000);
        var origin = new SkillTreeLayout.Point(0, 0);
        for (var weapon : SkillTreeLayout.WEAPONS) root(g, origin, SkillTreeLayout.weapon(weapon), 0xFF97826A);
        for (var node : SkillTreeLayout.NODES) {
            var before = SkillTree.byId(node.parent());
            var from = before == null ? SkillTreeLayout.weapon(node.weapon()) : SkillTreeLayout.node(before);
            int color = stats().hasTalent(node.id()) ? 0xFFEBC05A : SkillTree.available(stats(), node) ? 0xFF95AE7C : 0xFF746C64;
            root(g, from, SkillTreeLayout.node(node), color);
        }
        int faceSize = Math.max(6, Math.round(32 * zoom)), border = Math.max(1, Math.round(4 * zoom));
        int cx = screenX(origin), cy = screenY(origin);
        g.blitSprite(ResourceLocation.withDefaultNamespace("advancements/challenge_frame_obtained"), cx - faceSize / 2 - border, cy - faceSize / 2 - border, faceSize + border * 2, faceSize + border * 2);
        PlayerFaceRenderer.draw(g, minecraft.player.getSkin(), cx - faceSize / 2, cy - faceSize / 2, faceSize);
        g.disableScissor();
    }
    /** Raiz em degraus; o contorno escuro preserva a leitura das ligações como nas conquistas. */
    private void root(GuiGraphics g, SkillTreeLayout.Point from, SkillTreeLayout.Point to, int color) {
        int x1 = screenX(from), y1 = screenY(from), x2 = screenX(to), y2 = screenY(to);
        int midX = (x1 + x2) / 2, midY = (y1 + y2) / 2;
        for (int pass = 0; pass < 2; pass++) {
            int c = pass == 0 ? 0xFF10100F : color, thickness = pass == 0 ? 3 : 1;
            stroke(g, x1, y1, midX, y1, c, thickness);
            stroke(g, midX, y1, midX, midY, c, thickness);
            stroke(g, midX, midY, x2, midY, c, thickness);
            stroke(g, x2, midY, x2, y2, c, thickness);
        }
    }
    private static void stroke(GuiGraphics g, int x1, int y1, int x2, int y2, int color, int thickness) {
        int pad = thickness / 2;
        g.fill(Math.min(x1, x2) - pad, Math.min(y1, y2) - pad, Math.max(x1, x2) + pad + 1, Math.max(y1, y2) + pad + 1, color);
    }
    @Override public void render(GuiGraphics g, int mx, int my, float partial) {
        super.render(g, mx, my, partial);
        g.drawString(font, title, left + 10, top + 11, 0xFF303030, false);
        RpgUi.fit(g, font, Component.translatable("screen.solomod.skill_points", stats().getSkillPoints()),
                left + panelW - 160, top + 12, 86, 0xFF303030, 0.85F);
        Component name = selected != null ? selected.name() : inspectedWeapon != null ? inspectedWeapon.displayName()
                : Component.translatable("screen.solomod.tree_player", minecraft.player.getName());
        RpgUi.fit(g, font, name, left + 10, top + panelH - 51, panelW - 102, 0xFF303030, 0.9F);
        Component detail = selected != null ? Component.translatable("screen.solomod.node_cost", selected.cost(), selected.mana())
                : Component.translatable("screen.solomod.tree_hint");
        RpgUi.fit(g, font, detail, left + 10, top + panelH - 36, panelW - 102, 0xFF454545, 0.75F);
        RpgUi.fit(g, font, Component.translatable("screen.solomod.tree_income"), left + 10, top + panelH - 17, panelW - 20, 0xFF454545, 0.65F);
    }
    private boolean inMap(double x, double y) { return x >= mapX && x < mapX + mapW && y >= mapY && y < mapY + mapH - 24; }
    @Override public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        if (button == 0 && inMap(x, y)) { panX += dx / zoom; panY += dy / zoom; updateNodes(); return true; }
        return super.mouseDragged(x, y, button, dx, dy);
    }
    @Override public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (inMap(x, y)) { changeZoom((float) (zoom * Math.pow(1.18, vertical)), x, y); return true; }
        return super.mouseScrolled(x, y, horizontal, vertical);
    }
    @Override public boolean keyPressed(int key, int scan, int modifiers) {
        if (key == GLFW.GLFW_KEY_HOME) { centerMap(); return true; }
        if (key >= GLFW.GLFW_KEY_RIGHT && key <= GLFW.GLFW_KEY_UP) {
            if (key == GLFW.GLFW_KEY_LEFT) panX += 24 / zoom;
            if (key == GLFW.GLFW_KEY_RIGHT) panX -= 24 / zoom;
            if (key == GLFW.GLFW_KEY_UP) panY += 24 / zoom;
            if (key == GLFW.GLFW_KEY_DOWN) panY -= 24 / zoom;
            updateNodes(); return true;
        }
        return super.keyPressed(key, scan, modifiers);
    }
    private final class MapButton extends Button {
        private final WeaponClass weapon;
        private final SkillTree.Node node;
        MapButton(WeaponClass weapon, SkillTree.Node node) {
            super(0, 0, 26, 26, node == null ? weapon.displayName() : node.name(), b -> {
                selected = node; inspectedWeapon = weapon; updateNodes();
            }, Button.DEFAULT_NARRATION);
            this.weapon = weapon; this.node = node;
        }
        SkillTreeLayout.Point position() { return node == null ? SkillTreeLayout.weapon(weapon) : SkillTreeLayout.node(node); }
        @Override public boolean isMouseOver(double x, double y) { return inMap(x, y) && super.isMouseOver(x, y); }
        @Override public boolean mouseClicked(double x, double y, int button) { return inMap(x, y) && super.mouseClicked(x, y, button); }
        @Override protected void renderWidget(GuiGraphics g, int mx, int my, float partial) {
            if (!inMap(mx, my)) isHovered = false;
            boolean learned = node == null || stats().hasTalent(node.id());
            String frame = node == null || node.skill() == WeaponSkill.ELEMENTAL_BURST ? "challenge" : node.skill() != null ? "goal" : "task";
            g.enableScissor(mapX, mapY, mapX + mapW, mapY + mapH - 24);
            g.pose().pushPose(); g.pose().translate(getX(), getY(), 0);
            float scale = getWidth() / 26F; g.pose().scale(scale, scale, 1);
            g.blitSprite(ResourceLocation.withDefaultNamespace("advancements/" + frame + "_frame_" + (learned ? "obtained" : "unobtained")), 0, 0, 26, 26);
            g.renderItem(icon(), 5, 5);
            if ((node != null && selected == node) || (isHoveredOrFocused() && inMap(mx, my))) g.renderOutline(-1, -1, 28, 28, 0xFFFFFFFF);
            if (node != null && !learned && SkillTree.available(stats(), node)) g.fill(19, 19, 24, 24, 0xFF55DD55);
            g.pose().popPose(); g.disableScissor();
            setTooltip(Tooltip.create(description()));
        }
        private ItemStack icon() {
            if (node == null) return normalWeaponIcon();
            if (node.skill() == WeaponSkill.ELEMENTAL_BURST) return new ItemStack(Items.NETHER_STAR);
            if (node.skill() != null) return new ItemStack(Items.ENCHANTED_BOOK);
            if (node.id().equals("dagger.teleport")) return new ItemStack(Items.ENDER_PEARL);
            if (node.technique() != null) return new ItemStack(switch (node.technique()) {
                case CRESCENT -> Items.IRON_SWORD;
                case RISING -> Items.FEATHER;
                case CROSS -> Items.DIAMOND_SWORD;
                case RAPIER_DASH -> Items.SUGAR;
                case RAPIER_FLURRY -> Items.ARROW;
                case RAPIER_RETREAT -> Items.RABBIT_FOOT;
                case HAMMER_LEAP -> Items.ANVIL;
                case HAMMER_SWEEP -> Items.IRON_AXE;
                case HAMMER_QUAKE -> Items.POINTED_DRIPSTONE;
            });
            return normalWeaponIcon();
        }
        private ItemStack normalWeaponIcon() {
            return com.tarcitani.solomod.item.ModularWeaponItem.create(ModItems.get(weapon),
                    com.tarcitani.solomod.component.WeaponData.defaultFor(weapon));
        }
        private Component description() {
            if (node == null) return weapon.displayName().copy().append("\n").append(Component.translatable("screen.solomod.weapon_branch"));
            Component requirement = node.parent().isEmpty() ? Component.translatable("screen.solomod.root")
                    : Component.translatable("screen.solomod.node_parent", SkillTree.byId(node.parent()).name());
            Component detail = node.skill() != null ? Component.translatable("screen.solomod.tier_requirement", node.skill().minInt())
                    : Component.translatable("screen.solomod.class_requirement");
            String description = node.technique() != null ? node.technique().name().toLowerCase(java.util.Locale.ROOT)
                    : node.skill() != null ? node.skill().id() : node.id().substring(node.id().indexOf('.') + 1);
            return node.name().copy().append("\n").append(weapon.displayName()).append("\n")
                    .append(Component.translatable("talent_description.solomod." + description)).append("\n")
                    .append(Component.translatable("screen.solomod.node_cost", node.cost(), node.mana())).append("\n")
                    .append(requirement).append("\n").append(detail).append("\n")
                    .append(Component.translatable(stats().hasTalent(node.id()) ? "screen.solomod.learned" : SkillTree.available(stats(), node) ? "screen.solomod.available" : "screen.solomod.blocked"));
        }
    }
}
