package com.energystarcraft.screen;

import com.energystarcraft.menu.EnergyForgeMenu;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class EnergyForgeScreen extends AbstractContainerScreen<EnergyForgeMenu> {

    private static final NumberFormat NUM = NumberFormat.getNumberInstance(Locale.US);

    private static final int W = EnergyForgeMenu.GUI_W;
    private static final int H = EnergyForgeMenu.GUI_H;

    private static final int BAR_X = EnergyForgeMenu.BAR_X;
    private static final int BAR_Y = EnergyForgeMenu.BAR_Y;
    private static final int BAR_W = EnergyForgeMenu.BAR_W;
    private static final int BAR_H = EnergyForgeMenu.BAR_H;

    private static final int OUT_X = EnergyForgeMenu.OUTPUT_SLOT_X;
    private static final int OUT_Y = EnergyForgeMenu.OUTPUT_SLOT_Y;

    private static final int INV_START_X = EnergyForgeMenu.INV_START_X;
    private static final int INV_START_Y = EnergyForgeMenu.INV_START_Y;
    private static final int HOTBAR_Y    = EnergyForgeMenu.HOTBAR_Y;

    private static final int DIVIDER_Y = EnergyForgeMenu.DIVIDER_Y;

    private static final int BG_DARK        = 0xFF1A1226;
    private static final int BG_PANEL       = 0xFF3D283C;
    private static final int EDGE_BRIGHT    = 0xFFB8B8FF;
    private static final int EDGE_GLOW      = 0xFF9D4EE8;
    private static final int EDGE_DARK      = 0xFF1F1030;
    private static final int SLOT_BG        = 0xFF272800;
    private static final int SLOT_INNER     = 0xFF4C4C50;
    private static final int BAR_EMPTY      = 0xFF0E0515;
    private static final int BAR_FILL_LOW   = 0xFF7A2738;
    private static final int BAR_FILL_HI    = 0xFFB08EFF;
    private static final int BAR_DONE       = 0xFFFFD700;
    private static final int TEXT_LABEL     = 0xFF50FF9D;
    private static final int TEXT_VALUE     = 0xFFE8B8FF;
    private static final int TEXT_MUTED     = 0xFF7A3059;
    private static final int TEXT_READY     = 0xFFFFD700;
    private static final int TEXT_TITLE     = 0xFFFFD8FF;

    private static final int SPARKLE_COUNT = 14;
    private final Sparkle[] sparkles = new Sparkle[SPARKLE_COUNT];
    private final Random    random   = new Random();

    private float animTime;
    private float smoothedEnergyHeight;
    private long  lastTimeMs = System.currentTimeMillis();

    public EnergyForgeScreen(EnergyForgeMenu menu,
                             Inventory inventory,
                             Component title) {
        super(menu, inventory, title);
        this.imageWidth   = W;
        this.imageHeight  = H;
        this.titleLabelY     = -1000;
        this.inventoryLabelY = -1000;
        this.topPos += 20;
        for (int i = 0; i < SPARKLE_COUNT; i++) {
            sparkles[i] = new Sparkle(random);
        }
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos -= 10;
        this.smoothedEnergyHeight = menu.getScaledEnergy(BAR_H);
    }

    private void updateAnimations() {
        long  now          = System.currentTimeMillis();
        float deltaSeconds = (float) (now - lastTimeMs) / 1000f;
        lastTimeMs = now;
        if (deltaSeconds > 0.1f) deltaSeconds = 0.1f;

        animTime += deltaSeconds;

        float target = menu.getScaledEnergy(BAR_H);
        float speed  = Math.min(1f, 8f * deltaSeconds);
        smoothedEnergyHeight += (target - smoothedEnergyHeight) * speed;

        for (Sparkle s : sparkles) s.update(random, deltaSeconds);
    }

    @Override
    protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
        updateAnimations();
        int x = leftPos;
        int y = topPos;

        drawPanelBackground(g, x, y);
        drawSparkles(g, x, y);
        drawDivider(g, x, y);
        drawBar(g, x, y);

        drawSlot(g, x + OUT_X - 1, y + OUT_Y - 1, true);

        drawInventory(g, x, y);
        drawLabels(g, x, y);
    }

    private void drawPanelBackground(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + W, y + H, BG_DARK);

        for (int i = 0; i < 24; i++) {
            int alpha = 32 - i;
            if (alpha <= 0) continue;
            g.fill(x, y + i, x + W, y + i + 1,
                   (alpha << 24) | (EDGE_GLOW & 0xFFFFFF));
        }

        g.fill(x + 3, y + 3, x + W - 3, y + DIVIDER_Y - 3, BG_PANEL);

        for (int i = 0; i < 12; i++) {
            int alpha = 21 - i;
            if (alpha <= 0) continue;
            g.fill(x + 3, y + 3 + i, x + W - 3, y + 4 + i,
                   (alpha << 24) | 0xFFFFFF);
        }

        fancyBevel(g, x + 3, y + 3, W - 6, DIVIDER_Y - 6);
        fancyBevel(g, x,     y,     W,     H);
    }

    private void drawSparkles(GuiGraphics g, int ox, int oy) {
        for (Sparkle s : sparkles) {
            float life  = s.life / s.maxLife;
            float alpha = (float) Math.sin(life * Math.PI);
            if (alpha <= 0f) continue;

            int a     = (int) (alpha * 220f);
            int color = (a << 24) | (s.color & 0xFFFFFF);
            int px    = ox + 5 + (int) s.x;
            int py    = oy + 5 + (int) s.y;

            g.fill(px, py, px + 1, py + 1, color);
            if (alpha > 0.7f) {
                int dimA = (int) (alpha * 80f);
                int dim  = (dimA << 24) | (s.color & 0xFFFFFF);
                g.fill(px - 1, py,     px,     py + 1, dim);
                g.fill(px + 1, py,     px + 2, py + 1, dim);
                g.fill(px,     py - 1, px + 1, py,     dim);
                g.fill(px,     py + 1, px + 1, py + 2, dim);
            }
        }
    }

    private void drawDivider(GuiGraphics g, int x, int y) {
        g.fill(x + 3, y + DIVIDER_Y,     x + W - 3, y + DIVIDER_Y + 1, EDGE_DARK);
        g.fill(x + 3, y + DIVIDER_Y + 1, x + W - 3, y + DIVIDER_Y + 2, EDGE_GLOW);
    }

    private void drawBar(GuiGraphics g, int ox, int oy) {
        int x = ox + BAR_X;
        int y = oy + BAR_Y;

        g.fill(x - 2, y - 2, x + BAR_W + 2, y + BAR_H + 2, EDGE_DARK);
        g.fill(x - 1, y - 1, x + BAR_W + 1, y + BAR_H + 1, EDGE_GLOW);
        g.fill(x, y, x + BAR_W, y + BAR_H, BAR_EMPTY);

        int filled = Math.max(0, Math.min(BAR_H, Math.round(smoothedEnergyHeight)));
        if (filled > 0) {
            int pct = menu.getEnergyPercent();
            int fy  = y + BAR_H - filled;

            for (int row = 0; row < filled; row++) {
                float t     = (float) row / Math.max(1, filled);
                int   color = (pct >= 100)
                        ? gentlePulse(BAR_DONE)
                        : mix(BAR_FILL_LOW, BAR_FILL_HI, t);
                g.fill(x, fy + row, x + BAR_W, fy + row + 1, color);
            }

            int hl = mix(BAR_FILL_HI, 0xFFFFFFFF, 0.5f);
            g.fill(x, fy, x + 2, y + BAR_H, hl);
            int shadow = mix(BAR_FILL_LOW, 0xFF000000, 0.4f);
            g.fill(x + BAR_W - 2, fy, x + BAR_W, y + BAR_H, shadow);
        }

        for (int i = 1; i < 4; i++) {
            int ly = y + BAR_H - BAR_H * i / 4;
            g.fill(x, ly, x + BAR_W, ly + 1, 0x40FFFFFF);
        }
    }

    private void drawSlot(GuiGraphics g, int x, int y, boolean withGlow) {
        if (withGlow && (menu.getEnergyPercent() >= 100 || menu.getCraftingStatus() == 1)) {
            float p = (float) (Math.sin(animTime * 2.0) * 0.5 + 0.5);
            int   a = (int) (60f + p * 80f);
            g.fill(x - 2, y - 2, x + 22, y + 22, (a << 24) | 0xFFD700);
        }
        g.fill(x - 1, y - 1, x + 21, y + 21, EDGE_DARK);
        g.fill(x,      y,      x + 20, y + 1,  EDGE_GLOW);
        g.fill(x,      y,      x + 1,  y + 20, EDGE_GLOW);
        g.fill(x,      y + 19, x + 20, y + 20, EDGE_BRIGHT);
        g.fill(x + 19, y,      x + 20, y + 20, EDGE_BRIGHT);
        g.fill(x + 1, y + 1, x + 19, y + 19, SLOT_BG);
    }

    private void drawInventory(GuiGraphics g, int ox, int oy) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawInvSlot(g,
                        ox + INV_START_X + col * 18 - 1,
                        oy + INV_START_Y + row * 18 - 1);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawInvSlot(g,
                    ox + INV_START_X + col * 18 - 1,
                    oy + HOTBAR_Y - 1);
        }
    }

    private void drawInvSlot(GuiGraphics g, int x, int y) {
        g.fill(x - 1, y - 1, x + 19, y + 19, EDGE_DARK);
        g.fill(x,      y,      x + 18, y + 1,  EDGE_GLOW);
        g.fill(x,      y,      x + 1,  y + 18, EDGE_GLOW);
        g.fill(x,      y + 17, x + 18, y + 18, EDGE_BRIGHT);
        g.fill(x + 17, y,      x + 18, y + 18, EDGE_BRIGHT);
        g.fill(x + 1, y + 1, x + 17, y + 17, SLOT_INNER);
    }

    private void drawLabels(GuiGraphics g, int ox, int oy) {
        String titleStr = this.title.getString();
        int    tx       = ox + (W - this.font.width(titleStr)) / 2;
        g.drawString(this.font, titleStr, tx + 1, oy + 7, 0xFF000000, false);
        g.drawString(this.font, titleStr, tx,     oy + 6, TEXT_TITLE,  false);

        int textX = ox + BAR_X + BAR_W + 6;

        g.drawString(this.font, "ENERGY",
                textX, oy + BAR_Y, TEXT_LABEL, false);
        g.drawString(this.font, NUM.format(menu.getEnergyStored()),
                textX, oy + BAR_Y + 10, TEXT_VALUE, false);
        g.drawString(this.font, "/ " + NUM.format(menu.getMaxEnergyStored()),
                textX, oy + BAR_Y + 20, TEXT_MUTED, false);
        g.drawString(this.font, menu.getEnergyPercent() + "%",
                textX, oy + BAR_Y + 33, TEXT_VALUE, false);

        boolean ready       = menu.getEnergyPercent() >= 100;
        String  status      = ready ? "READY!" : "Charging...";
        int     statusColor = ready ? gentlePulse(TEXT_READY) : TEXT_MUTED;
        g.drawString(this.font, status,
                textX, oy + BAR_Y + 43, statusColor, false);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);

        if (this.isHovering(BAR_X, BAR_Y, BAR_W, BAR_H, mx, my)) {
            g.setTooltipForNextFrame(this.font,
                    List.of(
                            Component.literal(
                                            NUM.format(menu.getEnergyStored()) + " / " +
                                                    NUM.format(menu.getMaxEnergyStored()) + " FE")
                                    .withStyle(ChatFormatting.GOLD),
                            Component.literal(menu.getEnergyPercent() + "% charged")
                                    .withStyle(ChatFormatting.LIGHT_PURPLE),
                            Component.literal("350,000,000 FE = 1 Nether Star")
                                    .withStyle(ChatFormatting.DARK_PURPLE)),
                    Optional.empty(), mx, my);
        }

        boolean hoveringEmptyOutput =
                this.isHovering(OUT_X, OUT_Y, 16, 16, mx, my) &&
                this.menu.slots.get(0).getItem().isEmpty();

        if (hoveringEmptyOutput) {
            int           pct  = menu.getEnergyPercent();
            MutableComponent line = (pct >= 100)
                    ? Component.literal("Ready to craft!")
                               .withStyle(ChatFormatting.GOLD)
                    : Component.literal(
                               "Need " + NUM.format(
                                   menu.getMaxEnergyStored() -
                                   menu.getEnergyStored()) + " more FE")
                               .withStyle(ChatFormatting.LIGHT_PURPLE);
            g.setTooltipForNextFrame(this.font, line, mx, my);
        }

        this.renderTooltip(g, mx, my);
    }

    private int gentlePulse(int color) {
        float p = (float) (Math.sin(animTime * 1.5) * 0.5 + 0.5);
        int   a = (int) (220f + p * 35f);
        return (a << 24) | (color & 0xFFFFFF);
    }

    private void fancyBevel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x,         y,         x + w,     y + 1,     EDGE_BRIGHT);
        g.fill(x,         y,         x + 1,     y + h,     EDGE_BRIGHT);
        g.fill(x + 1,     y + 1,     x + w - 1, y + 2,     EDGE_GLOW);
        g.fill(x + 1,     y + 1,     x + 2,     y + h - 1, EDGE_GLOW);
        g.fill(x,         y + h - 1, x + w,     y + h,     EDGE_DARK);
        g.fill(x + w - 1, y,         x + w,     y + h,     EDGE_DARK);
    }

    private static int mix(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r  = (int) ((a >> 16 & 0xFF) + ((b >> 16 & 0xFF) - (a >> 16 & 0xFF)) * t);
        int gr = (int) ((a >>  8 & 0xFF) + ((b >>  8 & 0xFF) - (a >>  8 & 0xFF)) * t);
        int bl = (int) ((a       & 0xFF) + ((b       & 0xFF) - (a       & 0xFF)) * t);
        return 0xFF000000 | r << 16 | gr << 8 | bl;
    }

    private static class Sparkle {
        float x, y;
        float life, maxLife;
        int   color;

        private static final int[] COLORS = {
            TEXT_TITLE,
            TEXT_VALUE,
            BAR_FILL_HI,
            0xFFFFFFFF,
            BAR_DONE
        };

        Sparkle(Random r) { respawn(r); }

        void update(Random r, float dt) {
            life += dt;
            if (life >= maxLife) respawn(r);
        }

        void respawn(Random r) {
            x       = r.nextFloat() * 165f;
            y       = r.nextFloat() * 62f;
            life    = 0f;
            maxLife = 2f + r.nextFloat() * 3f;
            color   = COLORS[r.nextInt(COLORS.length)];
        }
    }
}