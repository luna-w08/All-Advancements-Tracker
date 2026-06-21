package dev.milow.aaigttracker.client.model;

public enum MenuBackgroundColor {
    CLASSIC(
            0xFF522A2A,
            0x92321818,
            0x99522A2A,
            0xA04A2626,
            0xD48C6B2A
    ),
    DEEPSLATE(
            0xFF3E4A55,
            0x92202B32,
            0x9933424C,
            0xA02A343D,
            0xD4526574
    ),
    FOREST(
            0xFF355B3D,
            0x92152418,
            0x99305A38,
            0xA0223E2A,
            0xD4557C3E
    ),
    AMETHYST(
            0xFF594275,
            0x92251A33,
            0x994D3A66,
            0xA03B2C50,
            0xD4745FA0
    ),
    GOLD(
            0xFF795A2E,
            0x922D2114,
            0x99694A25,
            0xA04E371D,
            0xD4927133
    );

    private final int swatchColor;
    private final int screenColor;
    private final int boardColor;
    private final int panelColor;
    private final int activeColor;

    MenuBackgroundColor(int swatchColor, int screenColor, int boardColor, int panelColor, int activeColor) {
        this.swatchColor = swatchColor;
        this.screenColor = screenColor;
        this.boardColor = boardColor;
        this.panelColor = panelColor;
        this.activeColor = activeColor;
    }

    public int swatchColor() {
        return this.swatchColor;
    }

    public int screenColor() {
        return this.screenColor;
    }

    public int boardColor() {
        return this.boardColor;
    }

    public int panelColor() {
        return this.panelColor;
    }

    public int activeColor() {
        return this.activeColor;
    }
}
