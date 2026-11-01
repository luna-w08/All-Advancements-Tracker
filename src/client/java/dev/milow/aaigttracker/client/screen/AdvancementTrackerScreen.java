package dev.milow.aaigttracker.client.screen;

import com.mojang.blaze3d.platform.InputConstants;

import dev.milow.aaigttracker.client.app.AllAdvancementsIgtTrackerClient;
import dev.milow.aaigttracker.client.data.ItemCompletionGateData;
import dev.milow.aaigttracker.client.data.TrackerDataRepository;
import dev.milow.aaigttracker.client.item.ItemProgressService;
import dev.milow.aaigttracker.client.manager.AdvancementTrackerManager;
import dev.milow.aaigttracker.client.model.AdvancementSnapshot;
import dev.milow.aaigttracker.client.model.CompletionVisibilityMode;
import dev.milow.aaigttracker.client.model.CriterionSnapshot;
import dev.milow.aaigttracker.client.model.ItemProgressSnapshot;
import dev.milow.aaigttracker.client.model.MenuBackgroundColor;
import dev.milow.aaigttracker.client.model.RequirementGroupSnapshot;
import dev.milow.aaigttracker.client.model.TrackerUiState;
import dev.milow.aaigttracker.client.model.WorldKey;
import dev.milow.aaigttracker.client.ui.model.TrackerScreenModels.AdvancementBoardEntry;
import dev.milow.aaigttracker.client.ui.model.TrackerScreenModels.BoardEntry;
import dev.milow.aaigttracker.client.ui.model.TrackerScreenModels.BoardTab;
import dev.milow.aaigttracker.client.ui.model.TrackerScreenModels.GridLayout;
import dev.milow.aaigttracker.client.ui.model.TrackerScreenModels.HoveredTile;
import dev.milow.aaigttracker.client.ui.model.TrackerScreenModels.ItemBoardEntry;
import dev.milow.aaigttracker.client.ui.model.TrackerScreenModels.PreviewLayout;
import dev.milow.aaigttracker.client.ui.model.TrackerScreenModels.PreviewOverlay;
import dev.milow.aaigttracker.client.ui.model.TrackerScreenModels.Rect;
import dev.milow.aaigttracker.client.ui.model.TrackerScreenModels.RequirementHitbox;
import dev.milow.aaigttracker.client.ui.model.TrackerScreenModels.RequirementMode;
import dev.milow.aaigttracker.client.ui.model.TrackerScreenModels.RequirementRenderData;
import dev.milow.aaigttracker.client.ui.model.TrackerScreenModels.RequirementSection;
import dev.milow.aaigttracker.client.ui.model.TrackerScreenModels.RequirementSectionLayout;
import dev.milow.aaigttracker.client.ui.model.TrackerScreenModels.RequirementViewport;
import dev.milow.aaigttracker.client.ui.model.TrackerScreenModels.TabButton;
import dev.milow.aaigttracker.client.ui.model.TrackerScreenModels.TabKind;
import dev.milow.aaigttracker.client.ui.model.TrackerScreenModels.TileHitbox;
import dev.milow.aaigttracker.client.ui.model.TrackerScreenModels.WaypointBoardEntry;
import dev.milow.aaigttracker.client.visual.BiomeSpriteSpec;
import dev.milow.aaigttracker.client.visual.CriterionIcon;
import dev.milow.aaigttracker.client.visual.EntityPreviewSpec;
import dev.milow.aaigttracker.client.visual.SupplementaryIconMode;
import dev.milow.aaigttracker.client.waypoint.WaypointService;
import dev.milow.aaigttracker.client.waypoint.WaypointSnapshot;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.state.EnderDragonRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.FrogRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class AdvancementTrackerScreen extends Screen {
    private static final AtomicInteger NEXT_PREVIEW_ENTITY_ID = new AtomicInteger(-1);
    private static final int OUTER_PADDING = 12;
    private static final int HEADER_Y = 12;
    private static final int BOARD_TOP = 40;
    private static final int BOARD_INNER_PADDING = 10;
    private static final int TAB_HEIGHT = 20;
    private static final int TAB_GAP = 6;
    private static final int CONTENT_GAP = 10;
    private static final int TILE_GAP = 6;
    private static final int DETAIL_HEIGHT = 152;
    private static final int DETAIL_INNER_PADDING = 10;
    private static final int TILE_TEXT_LINE_HEIGHT = 9;
    private static final int TILE_TEXT_GAP = 2;
    private static final int MIN_TILE_WIDTH = 132;
    private static final int MIN_TILE_HEIGHT = 40;
    private static final int EMERGENCY_MIN_TILE_WIDTH = 104;
    private static final int MAX_TILE_WIDTH = 220;
    private static final int MAX_TILE_HEIGHT = 46;
    private static final int REQUIREMENT_COLUMN_GAP = 10;
    private static final int MIN_REQUIREMENT_COLUMN_WIDTH = 136;
    private static final int REQUIREMENT_ENTRY_HEIGHT = 18;
    private static final int REQUIREMENT_ENTRY_GAP = 4;
    private static final int REQUIREMENT_HEADER_HEIGHT = 9;
    private static final int REQUIREMENT_HEADER_GAP = 8;
    private static final int REQUIREMENT_SECTION_GAP = 10;
    private static final int REQUIREMENT_GROUP_LABEL_GAP = 5;
    private static final int REQUIREMENT_SCROLLBAR_WIDTH = 6;
    private static final int REQUIREMENT_SCROLLBAR_GAP = 6;
    private static final int REQUIREMENT_SCROLL_STEP = REQUIREMENT_ENTRY_HEIGHT + REQUIREMENT_ENTRY_GAP;
    private static final int REQUIREMENT_ICON_SIZE = 16;
    private static final int REQUIREMENT_ENTITY_SLOT_WIDTH = 28;
    private static final int REQUIREMENT_BIOME_SLOT_WIDTH = REQUIREMENT_ICON_SIZE;
    private static final int REQUIREMENT_ICON_GAP = 5;
    private static final int TILE_SUBTITLE_SCISSOR_HEIGHT = 10;
    private static final int BREEDING_ICON_CYCLE_MILLIS = 1200;
    private static final long ITEM_SCAN_INTERVAL_MILLIS = 1000L;
    private static final int PREVIEW_PANEL_PADDING = 12;
    private static final float PREVIEW_MAX_WIDTH_RATIO = 0.60F;
    private static final float PREVIEW_MAX_HEIGHT_RATIO = 0.50F;
    private static final long PREVIEW_ROTATION_PERIOD_MILLIS = 8000L;
    private static final float PREVIEW_CAMERA_X_ROTATION_DEGREES = -18.0F;
    private static final int PREVIEW_CLOSE_SIZE = 14;
    private static final int PREVIEW_NAV_WIDTH = 24;
    private static final int PREVIEW_NAV_HEIGHT = 42;
    private static final int PREVIEW_ITEM_BASE_SIZE = 16;
    private static final int PREVIEW_ITEM_TARGET_SIZE = 96;
    private static final int BIOME_SPRITE_SIZE = 16;
    private static final int BIOME_SPRITE_LOADS_PER_FRAME = 2;
    private static final double MARQUEE_PIXELS_PER_MILLISECOND = 0.045D;
    private static final int MARQUEE_LOOP_GAP = 20;
    private static final int SETTINGS_BUTTON_WIDTH = 68;
    private static final int SETTINGS_BUTTON_HEIGHT = 18;
    private static final int SETTINGS_PANEL_WIDTH = 292;
    private static final int SETTINGS_PANEL_HEIGHT = 146;
    private static final int SETTINGS_PANEL_PADDING = 10;
    private static final int SETTINGS_SEGMENT_HEIGHT = 18;
    private static final int SETTINGS_SWATCH_SIZE = 16;
    private static final int SETTINGS_SWATCH_GAP = 7;
    private static final int SETTINGS_TOGGLE_HEIGHT = 16;
    private static final Identifier ITEMS_TAB_ID = Identifier.fromNamespaceAndPath("aaigttracker", "items");
    private static final Identifier WAYPOINTS_TAB_ID = Identifier.fromNamespaceAndPath("aaigttracker", "waypoints");
    private static final int TEXT_PRIMARY = 0xFFFFF8EE;
    private static final int TEXT_HEADER = 0xFFFFF0DA;
    private static final int TEXT_SECONDARY = 0xFFDCCAB6;
    private static final int TEXT_MUTED = 0xFFD9C2AA;
    private static final int TEXT_COUNT = 0xFFF3DFC1;
    private static final int TEXT_BODY = 0xFFE7D8C8;
    private static final int TEXT_SUCCESS = 0xFFA5E08B;
    private static final int TEXT_COMPLETED_TITLE = 0xFF16220E;
    private final AdvancementTrackerManager trackerManager;
    private final Set<Identifier> drawnBiomeSprites = new HashSet<>();
    private int biomeSpriteLoadsThisFrame;
    private final ItemProgressService itemProgressService = new ItemProgressService();
    private final WaypointService waypointService = AllAdvancementsIgtTrackerClient.getWaypointService();

    private int boardX;
    private int boardY;
    private int boardWidth;
    private int boardHeight;
    private Identifier activeRootId;
    private Identifier selectedAdvancementId;
    private Identifier hoveredSubtitleAdvancementId;
    private long hoveredSubtitleStartedAt;
    private long currentRenderMillis;
    private Identifier requirementScrollAdvancementId;
    private int requirementScrollOffset;
    private long nextDataRefreshNanos;
    private List<AdvancementSnapshot> cachedEntries = List.of();
    private List<ItemProgressSnapshot> cachedItemEntries = List.of();
    private List<WaypointSnapshot> cachedWaypoints = List.of();
    private List<BoardTab> cachedTabs = List.of();
    private List<TabButton> renderedTabButtons = List.of();
    private List<TileHitbox> renderedTileHitboxes = List.of();
    private List<WaypointActionHitbox> renderedWaypointActionHitboxes = List.of();
    private List<SettingsActionHitbox> renderedSettingsActionHitboxes = List.of();
    private List<RequirementHitbox> renderedRequirementHitboxes = List.of();
    private RequirementViewport renderedRequirementViewport;
    private PreviewOverlay previewOverlay;
    private WaypointInputOverlay waypointInputOverlay;
    private boolean settingsMenuOpen;
    private Rect settingsButtonRect;
    private Rect settingsPanelRect;
    private Boolean debugOverlayWasVisible;
    private long nextItemScanAt;
    private final Map<EntityPreviewSpec, LivingEntity> previewEntities = new HashMap<>();

    public AdvancementTrackerScreen(AdvancementTrackerManager trackerManager) {
        super(Component.translatable("screen.aaigttracker.title"));
        this.trackerManager = trackerManager;
    }

    @Override
    protected void init() {
        this.hideDebugOverlay();
        TrackerUiState trackerUiState = AllAdvancementsIgtTrackerClient.getTrackerUiState(this.currentWorldKey());
        if (this.activeRootId == null) {
            this.activeRootId = trackerUiState.lastTrackerRootId();
        }
        if (this.selectedAdvancementId == null) {
            this.selectedAdvancementId = trackerUiState.lastSelectedAdvancementId();
        }
        this.refreshData();
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void removed() {
        this.minecraft.textInputManager().stopTextInput(this);
        this.rememberCurrentState();
        this.restoreDebugOverlay();
        super.removed();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.biomeSpriteLoadsThisFrame = 0;
        this.refreshData(false);
        this.currentRenderMillis = System.currentTimeMillis();
        this.extractTransparentBackground(guiGraphics);
        MenuBackgroundColor backgroundColor = this.menuBackgroundColor();
        guiGraphics.fill(0, 0, this.width, this.height, backgroundColor.screenColor());

        guiGraphics.text(this.font, this.title, OUTER_PADDING, HEADER_Y, TEXT_HEADER, false);
        this.renderSettingsButton(guiGraphics, mouseX, mouseY);

        Component countComponent = Component.translatable(
                "screen.aaigttracker.complete_count",
                this.trackerManager.getCompletedCount(),
                this.trackerManager.getTotalCount()
        );
        guiGraphics.text(
                this.font,
                countComponent,
                (this.width - this.font.width(countComponent)) / 2,
                HEADER_Y - 1,
                TEXT_COUNT,
                false
        );

        Component currentTimeComponent = Component.literal(
                this.trackerManager.getLastTimer().available()
                        ? formatMillis(this.trackerManager.getLastTimer().millis())
                        : formatMillis(null)
        );
        guiGraphics.text(
                this.font,
                currentTimeComponent,
                (this.width - this.font.width(currentTimeComponent)) / 2,
                HEADER_Y + 10,
                TEXT_SECONDARY,
                false
        );

        guiGraphics.fill(OUTER_PADDING, BOARD_TOP - 8, this.width - OUTER_PADDING, BOARD_TOP - 7, 0x8CA06D6D);

        this.boardX = OUTER_PADDING;
        this.boardY = BOARD_TOP;
        this.boardWidth = this.width - OUTER_PADDING * 2;
        this.boardHeight = this.height - BOARD_TOP - OUTER_PADDING;

        guiGraphics.fill(this.boardX, this.boardY, this.boardX + this.boardWidth, this.boardY + this.boardHeight, backgroundColor.boardColor());
        guiGraphics.outline(this.boardX, this.boardY, this.boardWidth, this.boardHeight, 0xC48E6565);

        if (this.cachedTabs.isEmpty()) {
            this.renderedTabButtons = List.of();
            this.renderedTileHitboxes = List.of();
            this.renderedWaypointActionHitboxes = List.of();
            this.renderedSettingsActionHitboxes = new ArrayList<>();
            this.renderedRequirementHitboxes = List.of();
            this.renderedRequirementViewport = null;
            Component emptyMessage = this.cachedEntries.isEmpty()
                    ? this.trackerManager.getStatusMessage()
                    : Component.translatable("screen.aaigttracker.filter.empty");
            guiGraphics.centeredText(
                    this.font,
                    emptyMessage,
                    this.boardX + this.boardWidth / 2,
                    this.boardY + this.boardHeight / 2 - 4,
                    TEXT_COUNT
            );
            if (this.settingsMenuOpen) {
                this.renderSettingsMenu(guiGraphics, mouseX, mouseY);
            }
            super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
            return;
        }

        this.renderedTabButtons = this.renderTabs(guiGraphics, mouseX, mouseY);

        BoardTab activeTab = this.getActiveTab();
        if (activeTab == null) {
            this.renderedRequirementHitboxes = List.of();
            this.renderedSettingsActionHitboxes = new ArrayList<>();
            this.renderedRequirementViewport = null;
            if (this.settingsMenuOpen) {
                this.renderSettingsMenu(guiGraphics, mouseX, mouseY);
            }
            super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
            return;
        }

        boolean modalOpen = this.previewOverlay != null || this.waypointInputOverlay != null || this.settingsMenuOpen;
        int interactionMouseX = !modalOpen ? mouseX : Integer.MIN_VALUE / 4;
        int interactionMouseY = !modalOpen ? mouseY : Integer.MIN_VALUE / 4;
        int contentX = this.boardX + BOARD_INNER_PADDING;
        int contentWidth = this.boardWidth - BOARD_INNER_PADDING * 2;
        int tabsBottom = this.boardY + BOARD_INNER_PADDING + TAB_HEIGHT;
        int gridY = tabsBottom + CONTENT_GAP;
        int contentBottom = this.boardY + this.boardHeight - BOARD_INNER_PADDING;
        BoardEntry selectedEntry = this.getSelectedEntry(activeTab);
        int detailHeight = this.detailPanelHeight(selectedEntry);
        GridLayout layout = this.computeGridLayout(
                activeTab.entries().size(),
                contentWidth,
                this.gridHeight(gridY, contentBottom, detailHeight)
        );
        BoardEntry hoveredEntry = this.findHoveredEntry(
                activeTab,
                interactionMouseX,
                interactionMouseY,
                contentX,
                gridY,
                contentWidth,
                this.gridHeight(gridY, contentBottom, detailHeight),
                layout
        );
        BoardEntry detailEntry = hoveredEntry != null ? hoveredEntry : selectedEntry;
        int resolvedDetailHeight = this.detailPanelHeight(detailEntry);
        if (resolvedDetailHeight != detailHeight) {
            detailHeight = resolvedDetailHeight;
            layout = this.computeGridLayout(
                    activeTab.entries().size(),
                    contentWidth,
                    this.gridHeight(gridY, contentBottom, detailHeight)
            );
            hoveredEntry = this.findHoveredEntry(
                    activeTab,
                    interactionMouseX,
                    interactionMouseY,
                    contentX,
                    gridY,
                    contentWidth,
                    this.gridHeight(gridY, contentBottom, detailHeight),
                    layout
            );
            detailEntry = hoveredEntry != null ? hoveredEntry : selectedEntry;
        }

        int gridHeight = this.gridHeight(gridY, contentBottom, detailHeight);
        int detailY = detailHeight > 0 ? contentBottom - detailHeight : contentBottom;
        this.renderedTileHitboxes = new ArrayList<>(activeTab.entries().size());
        HoveredTile hoveredTile = this.renderGrid(
                guiGraphics,
                interactionMouseX,
                interactionMouseY,
                activeTab,
                contentX,
                gridY,
                contentWidth,
                gridHeight,
                layout
        );
        if (hoveredTile == null) {
            this.updateHoveredSubtitleState(null);
        }

        this.renderedRequirementHitboxes = new ArrayList<>();
        this.renderedWaypointActionHitboxes = new ArrayList<>();
        this.renderedSettingsActionHitboxes = new ArrayList<>();
        if (detailHeight > 0) {
            BoardEntry renderedDetailEntry = hoveredTile != null ? hoveredTile.entry() : detailEntry;
            this.renderDetailPanel(guiGraphics, renderedDetailEntry, contentX, detailY, contentWidth, detailHeight, interactionMouseX, interactionMouseY);
        } else {
            this.renderedRequirementViewport = null;
            this.requirementScrollAdvancementId = null;
            this.requirementScrollOffset = 0;
        }
        if (this.previewOverlay == null && this.waypointInputOverlay == null) {
            this.renderRequirementTooltip(guiGraphics, mouseX, mouseY);
        } else {
            if (this.previewOverlay != null) {
                this.renderPreviewOverlay(guiGraphics, mouseX, mouseY);
            }
            if (this.waypointInputOverlay != null) {
                this.renderWaypointInputOverlay(guiGraphics);
            }
        }
        if (this.settingsMenuOpen && this.previewOverlay == null && this.waypointInputOverlay == null) {
            this.renderSettingsMenu(guiGraphics, mouseX, mouseY);
        }

        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        KeyMapping openTrackerKey = AllAdvancementsIgtTrackerClient.getOpenTrackerKey();
        if (openTrackerKey != null && openTrackerKey.matches(event)) {
            this.minecraft.setScreenAndShow(null);
            return true;
        }

        if (this.previewOverlay != null) {
            if (key == InputConstants.KEY_ESCAPE) {
                this.closePreviewOverlay();
                return true;
            }
            if (key == InputConstants.KEY_LEFT) {
                this.navigatePreview(-1);
                return true;
            }
            if (key == InputConstants.KEY_RIGHT) {
                this.navigatePreview(1);
                return true;
            }
            return true;
        }

        if (this.waypointInputOverlay != null) {
            if (key == InputConstants.KEY_ESCAPE) {
                this.waypointInputOverlay = null;
                this.minecraft.textInputManager().stopTextInput(this);
                return true;
            }
            if (key == InputConstants.KEY_RETURN || key == InputConstants.KEY_NUMPADENTER) {
                this.commitWaypointInput();
                return true;
            }
            if (key == InputConstants.KEY_BACKSPACE) {
                this.backspaceWaypointInput();
                return true;
            }
            return true;
        }

        if (this.settingsMenuOpen) {
            if (key == InputConstants.KEY_ESCAPE) {
                this.settingsMenuOpen = false;
            }
            return true;
        }

        if (key == InputConstants.KEY_F3) {
            return true;
        }

        if (key == InputConstants.KEY_LEFT) {
            this.cycleTab(-1);
            return true;
        }

        if (key == InputConstants.KEY_RIGHT) {
            this.cycleTab(1);
            return true;
        }

        if (key >= InputConstants.KEY_1 && key <= InputConstants.KEY_9) {
            int tabIndex = key - InputConstants.KEY_1;
            if (tabIndex < this.cachedTabs.size()) {
                this.selectTab(tabIndex);
                return true;
            }
        }

        return super.keyPressed(event);
    }

    @Override
    protected void insertText(String text, boolean overwrite) {
        if (this.waypointInputOverlay != null) {
            this.appendWaypointInput(text);
            return;
        }

        super.insertText(text, overwrite);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (this.waypointInputOverlay != null) {
            if (event.isAllowedChatCharacter()) {
                this.appendWaypointInput(event.codepointAsString());
            }
            return true;
        }

        return super.charTyped(event);
    }

    @Override
    public boolean isInputCaptured() {
        return this.waypointInputOverlay != null || super.isInputCaptured();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.previewOverlay != null) {
            return true;
        }

        if (this.waypointInputOverlay != null) {
            return true;
        }

        if (this.settingsMenuOpen) {
            return true;
        }

        if (this.renderedRequirementViewport != null
                && this.renderedRequirementViewport.maxScroll() > 0
                && this.renderedRequirementViewport.contains(mouseX, mouseY)) {
            int delta = (int) Math.round(scrollY * REQUIREMENT_SCROLL_STEP);
            this.requirementScrollOffset = clamp(this.requirementScrollOffset - delta, 0, this.renderedRequirementViewport.maxScroll());
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) {
            return super.mouseClicked(event, doubleClick);
        }

        if (this.previewOverlay != null) {
            PreviewLayout layout = this.previewLayout();
            if (layout != null) {
                if (layout.closeButton().contains(event.x(), event.y())) {
                    this.closePreviewOverlay();
                    return true;
                }
                if (this.previewOverlay.hasMultiple() && layout.leftArrow().contains(event.x(), event.y())) {
                    this.navigatePreview(-1);
                    return true;
                }
                if (this.previewOverlay.hasMultiple() && layout.rightArrow().contains(event.x(), event.y())) {
                    this.navigatePreview(1);
                    return true;
                }
            }
            return true;
        }

        if (this.waypointInputOverlay != null) {
            return true;
        }

        if (this.settingsButtonRect != null && this.settingsButtonRect.contains(event.x(), event.y())) {
            this.settingsMenuOpen = !this.settingsMenuOpen;
            return true;
        }

        if (this.settingsMenuOpen) {
            for (SettingsActionHitbox hitbox : this.renderedSettingsActionHitboxes) {
                if (hitbox.rect().contains(event.x(), event.y())) {
                    this.activateSettingsAction(hitbox);
                    return true;
                }
            }

            if (this.settingsPanelRect != null && this.settingsPanelRect.contains(event.x(), event.y())) {
                return true;
            }

            this.settingsMenuOpen = false;
            return true;
        }

        for (WaypointActionHitbox hitbox : this.renderedWaypointActionHitboxes) {
            if (hitbox.rect().contains(event.x(), event.y())) {
                this.activateWaypointAction(hitbox.action(), hitbox.waypointId());
                return true;
            }
        }

        for (TabButton button : this.renderedTabButtons) {
            if (inside(event.x(), event.y(), button.x(), button.y(), button.width(), button.height())) {
                this.selectTab(button.index());
                return true;
            }
        }

        for (RequirementHitbox hitbox : this.renderedRequirementHitboxes) {
            if (hitbox.contains(event.x(), event.y()) && this.openPreview(hitbox.criterion())) {
                return true;
            }
        }

        for (TileHitbox tileHitbox : this.renderedTileHitboxes) {
            if (inside(event.x(), event.y(), tileHitbox.x(), tileHitbox.y(), tileHitbox.width(), tileHitbox.height())) {
                this.selectedAdvancementId = tileHitbox.entry().id();
                this.rememberCurrentState();
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    private void refreshData() {
        this.refreshData(true);
    }

    private void refreshData(boolean force) {
        List<AdvancementSnapshot> snapshots = this.trackerManager.getSnapshots();
        if (!force && this.cachedEntries == snapshots && System.nanoTime() < this.nextDataRefreshNanos) {
            return;
        }
        this.nextDataRefreshNanos = System.nanoTime() + 200_000_000L;
        this.cachedEntries = snapshots;
        this.refreshItemEntries(false);
        this.cachedWaypoints = this.waypointService.waypoints(this.currentWorldKey());
        this.cachedTabs = this.buildTabs(
                this.visibleEntries(this.cachedEntries),
                this.visibleItemEntries(this.cachedItemEntries),
                this.cachedWaypoints
        );

        if (this.cachedTabs.isEmpty()) {
            this.activeRootId = null;
            this.selectedAdvancementId = null;
            this.rememberCurrentState();
            return;
        }

        boolean activeTabStillExists = this.activeRootId != null
                && this.cachedTabs.stream().anyMatch(tab -> tab.rootId().equals(this.activeRootId));
        if (!activeTabStillExists) {
            this.activeRootId = this.cachedTabs.get(0).rootId();
        }

        BoardTab activeTab = this.getActiveTab();
        boolean selectedStillVisible = this.selectedAdvancementId != null
                && activeTab != null
                && activeTab.entries().stream().anyMatch(entry -> entry.id().equals(this.selectedAdvancementId));
        if (!selectedStillVisible) {
            this.selectedAdvancementId = null;
        }

        this.rememberCurrentState();
    }

    private List<AdvancementSnapshot> visibleEntries(List<AdvancementSnapshot> entries) {
        CompletionVisibilityMode visibilityMode = this.visibilityMode();
        if (!visibilityMode.hidesCompletedAdvancements()) {
            return entries;
        }

        List<AdvancementSnapshot> visibleEntries = new ArrayList<>(entries.size());
        for (AdvancementSnapshot snapshot : entries) {
            if (!snapshot.done()) {
                visibleEntries.add(snapshot);
            }
        }
        return List.copyOf(visibleEntries);
    }

    private List<ItemProgressSnapshot> visibleItemEntries(List<ItemProgressSnapshot> entries) {
        CompletionVisibilityMode visibilityMode = this.visibilityMode();
        List<ItemProgressSnapshot> visibleEntries = new ArrayList<>(entries.size());
        for (ItemProgressSnapshot snapshot : entries) {
            if (this.isItemGateComplete(snapshot.id())) {
                continue;
            }
            if (!visibilityMode.hidesCompletedAdvancements() || !snapshot.done()) {
                visibleEntries.add(snapshot);
            }
        }
        return List.copyOf(visibleEntries);
    }

    private boolean isItemGateComplete(Identifier itemEntryId) {
        ItemCompletionGateData gate = TrackerDataRepository.current().itemObjectives().gate(itemEntryId);
        return gate != null && gate.isComplete(this.cachedEntries);
    }

    private List<BoardTab> buildTabs(
            List<AdvancementSnapshot> entries,
            List<ItemProgressSnapshot> itemEntries,
            List<WaypointSnapshot> waypoints
    ) {
        Map<Identifier, List<AdvancementSnapshot>> groupedEntries = new LinkedHashMap<>();
        for (AdvancementSnapshot snapshot : entries) {
            groupedEntries.computeIfAbsent(snapshot.rootId(), ignored -> new ArrayList<>()).add(snapshot);
        }

        List<BoardTab> tabs = new ArrayList<>(groupedEntries.size() + 1);
        for (Map.Entry<Identifier, List<AdvancementSnapshot>> entry : groupedEntries.entrySet()) {
            List<AdvancementSnapshot> snapshots = entry.getValue();
            if (snapshots.isEmpty()) {
                continue;
            }

            List<BoardEntry> boardEntries = new ArrayList<>(snapshots.size());
            for (AdvancementSnapshot snapshot : snapshots) {
                boardEntries.add(new AdvancementBoardEntry(snapshot));
            }

            tabs.add(new BoardTab(
                    entry.getKey(),
                    snapshots.get(0).rootTitle(),
                    snapshots.get(0).rootIcon().copy(),
                    List.copyOf(boardEntries),
                    TabKind.ADVANCEMENTS
            ));
        }

        List<BoardEntry> itemBoardEntries = new ArrayList<>(itemEntries.size());
        for (ItemProgressSnapshot snapshot : itemEntries) {
            itemBoardEntries.add(new ItemBoardEntry(snapshot));
        }

        tabs.add(new BoardTab(
                ITEMS_TAB_ID,
                Component.translatable("screen.aaigttracker.tab.items"),
                Items.BUNDLE.getDefaultInstance(),
                List.copyOf(itemBoardEntries),
                TabKind.ITEMS
        ));

        List<BoardEntry> waypointBoardEntries = new ArrayList<>(waypoints.size());
        for (WaypointSnapshot waypoint : waypoints) {
            waypointBoardEntries.add(new WaypointBoardEntry(waypoint));
        }

        tabs.add(new BoardTab(
                WAYPOINTS_TAB_ID,
                Component.translatable("screen.aaigttracker.tab.waypoints"),
                Items.COMPASS.getDefaultInstance(),
                List.copyOf(waypointBoardEntries),
                TabKind.WAYPOINTS
        ));

        return List.copyOf(tabs);
    }

    private List<TabButton> renderTabs(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        List<TabButton> buttons = new ArrayList<>(this.cachedTabs.size());
        int cursorX = this.boardX + BOARD_INNER_PADDING;
        int tabY = this.boardY + BOARD_INNER_PADDING;

        for (int index = 0; index < this.cachedTabs.size(); index++) {
            BoardTab tab = this.cachedTabs.get(index);
            String label = tab.title().getString();
            int width = this.font.width(label) + 34;
            boolean active = tab.rootId().equals(this.activeRootId);
            boolean hovered = inside(mouseX, mouseY, cursorX, tabY, width, TAB_HEIGHT);
            int background = active ? this.menuBackgroundColor().activeColor() : hovered ? 0xC5724646 : 0x9B603737;
            int border = active ? 0xFFF0D3A3 : 0xB1855C5C;

            guiGraphics.fill(cursorX, tabY, cursorX + width, tabY + TAB_HEIGHT, background);
            guiGraphics.outline(cursorX, tabY, width, TAB_HEIGHT, border);
            if (!tab.icon().isEmpty()) {
                guiGraphics.item(tab.icon(), cursorX + 5, tabY + 2);
            }
            guiGraphics.text(this.font, label, cursorX + 24, tabY + 6, TEXT_PRIMARY, false);
            buttons.add(new TabButton(index, cursorX, tabY, width, TAB_HEIGHT));
            cursorX += width + TAB_GAP;
        }

        String hint = this.visibilityMode().label().getString();
        guiGraphics.text(
                this.font,
                hint,
                this.boardX + this.boardWidth - BOARD_INNER_PADDING - this.font.width(hint),
                tabY + 6,
                TEXT_MUTED,
                false
        );

        return List.copyOf(buttons);
    }

    private void renderSettingsButton(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        int x = this.width - OUTER_PADDING - SETTINGS_BUTTON_WIDTH;
        int y = HEADER_Y - 2;
        this.settingsButtonRect = new Rect(x, y, SETTINGS_BUTTON_WIDTH, SETTINGS_BUTTON_HEIGHT);
        boolean hovered = this.settingsButtonRect.contains(mouseX, mouseY);
        int background = this.settingsMenuOpen ? this.menuBackgroundColor().activeColor() : hovered ? 0xC5724646 : 0x9B603737;
        guiGraphics.fill(x, y, x + SETTINGS_BUTTON_WIDTH, y + SETTINGS_BUTTON_HEIGHT, background);
        guiGraphics.outline(x, y, SETTINGS_BUTTON_WIDTH, SETTINGS_BUTTON_HEIGHT, this.settingsMenuOpen || hovered ? 0xFFF0D3A3 : 0xB1855C5C);
        guiGraphics.centeredText(
                this.font,
                Component.translatable("screen.aaigttracker.settings"),
                x + SETTINGS_BUTTON_WIDTH / 2,
                y + 5,
                TEXT_PRIMARY
        );
    }

    private void renderSettingsMenu(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        this.renderedSettingsActionHitboxes.clear();
        int panelX = this.width - OUTER_PADDING - SETTINGS_PANEL_WIDTH;
        int panelY = HEADER_Y + SETTINGS_BUTTON_HEIGHT + 5;
        this.settingsPanelRect = new Rect(panelX, panelY, SETTINGS_PANEL_WIDTH, SETTINGS_PANEL_HEIGHT);
        int panelRight = panelX + SETTINGS_PANEL_WIDTH;
        MenuBackgroundColor backgroundColor = this.menuBackgroundColor();

        guiGraphics.fill(panelX, panelY, panelRight, panelY + SETTINGS_PANEL_HEIGHT, backgroundColor.panelColor() | 0xE0000000);
        guiGraphics.outline(panelX, panelY, SETTINGS_PANEL_WIDTH, SETTINGS_PANEL_HEIGHT, 0xFFF0D3A3);
        guiGraphics.text(
                this.font,
                Component.translatable("screen.aaigttracker.settings.title"),
                panelX + SETTINGS_PANEL_PADDING,
                panelY + 7,
                TEXT_HEADER,
                false
        );

        int contentX = panelX + SETTINGS_PANEL_PADDING;
        int contentWidth = SETTINGS_PANEL_WIDTH - SETTINGS_PANEL_PADDING * 2;
        int rowY = panelY + 24;
        guiGraphics.text(
                this.font,
                Component.translatable("screen.aaigttracker.settings.visibility"),
                contentX,
                rowY + 5,
                TEXT_MUTED,
                false
        );

        int segmentsX = contentX + 70;
        int segmentGap = 4;
        int segmentWidth = (panelRight - SETTINGS_PANEL_PADDING - segmentsX - segmentGap * 2) / 3;
        CompletionVisibilityMode[] modes = CompletionVisibilityMode.values();
        for (int index = 0; index < modes.length; index++) {
            CompletionVisibilityMode mode = modes[index];
            Rect rect = new Rect(
                    segmentsX + index * (segmentWidth + segmentGap),
                    rowY,
                    segmentWidth,
                    SETTINGS_SEGMENT_HEIGHT
            );
            boolean active = this.visibilityMode() == mode;
            boolean hovered = rect.contains(mouseX, mouseY);
            guiGraphics.fill(rect.x(), rect.y(), rect.right(), rect.bottom(), active ? backgroundColor.activeColor() : hovered ? 0xC5724646 : 0x9B603737);
            guiGraphics.outline(rect.x(), rect.y(), rect.width(), rect.height(), active || hovered ? 0xFFF0D3A3 : 0xB1855C5C);
            guiGraphics.centeredText(
                    this.font,
                    Component.literal(this.ellipsize(mode.shortLabel().getString(), rect.width() - 6)),
                    rect.x() + rect.width() / 2,
                    rect.y() + 5,
                    active ? TEXT_HEADER : TEXT_PRIMARY
            );
            this.renderedSettingsActionHitboxes.add(SettingsActionHitbox.visibility(rect, mode));
        }

        rowY += 27;
        guiGraphics.text(
                this.font,
                Component.translatable("screen.aaigttracker.settings.background"),
                contentX,
                rowY + 4,
                TEXT_MUTED,
                false
        );
        int swatchX = contentX + 86;
        for (MenuBackgroundColor color : MenuBackgroundColor.values()) {
            Rect rect = new Rect(swatchX, rowY, SETTINGS_SWATCH_SIZE, SETTINGS_SWATCH_SIZE);
            boolean active = color == backgroundColor;
            boolean hovered = rect.contains(mouseX, mouseY);
            guiGraphics.fill(rect.x(), rect.y(), rect.right(), rect.bottom(), color.swatchColor());
            guiGraphics.outline(rect.x(), rect.y(), rect.width(), rect.height(), active || hovered ? 0xFFFFF0DA : 0xB1855C5C);
            if (active) {
                guiGraphics.outline(rect.x() - 2, rect.y() - 2, rect.width() + 4, rect.height() + 4, 0xFFF0D3A3);
            }
            this.renderedSettingsActionHitboxes.add(SettingsActionHitbox.background(rect, color));
            swatchX += SETTINGS_SWATCH_SIZE + SETTINGS_SWATCH_GAP;
        }

        rowY += 25;
        Rect noFogRect = new Rect(contentX, rowY, contentWidth, SETTINGS_TOGGLE_HEIGHT);
        this.renderSettingsToggle(
                guiGraphics,
                noFogRect,
                Component.translatable("screen.aaigttracker.settings.no_fog"),
                AllAdvancementsIgtTrackerClient.isNoFogEnabled(),
                noFogRect.contains(mouseX, mouseY)
        );
        this.renderedSettingsActionHitboxes.add(SettingsActionHitbox.simple(noFogRect, SettingsAction.TOGGLE_NO_FOG));

        rowY += 20;
        Rect noDarknessRect = new Rect(contentX, rowY, contentWidth, SETTINGS_TOGGLE_HEIGHT);
        this.renderSettingsToggle(
                guiGraphics,
                noDarknessRect,
                Component.translatable("screen.aaigttracker.settings.no_darkness"),
                AllAdvancementsIgtTrackerClient.isNoDarknessEnabled(),
                noDarknessRect.contains(mouseX, mouseY)
        );
        this.renderedSettingsActionHitboxes.add(SettingsActionHitbox.simple(noDarknessRect, SettingsAction.TOGGLE_NO_DARKNESS));

        rowY += 20;
        Rect fullbrightRect = new Rect(contentX, rowY, contentWidth, SETTINGS_TOGGLE_HEIGHT);
        this.renderSettingsToggle(
                guiGraphics,
                fullbrightRect,
                Component.translatable("screen.aaigttracker.settings.fullbright"),
                AllAdvancementsIgtTrackerClient.isFullbrightEnabled(),
                fullbrightRect.contains(mouseX, mouseY)
        );
        this.renderedSettingsActionHitboxes.add(SettingsActionHitbox.simple(fullbrightRect, SettingsAction.TOGGLE_FULLBRIGHT));
    }

    private void renderSettingsToggle(
            GuiGraphicsExtractor guiGraphics,
            Rect rect,
            Component label,
            boolean enabled,
            boolean hovered
    ) {
        guiGraphics.fill(rect.x(), rect.y(), rect.right(), rect.bottom(), hovered ? 0x526A4545 : 0x33351E1E);
        int boxSize = 12;
        int boxX = rect.x() + 1;
        int boxY = rect.y() + 2;
        guiGraphics.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, enabled ? this.menuBackgroundColor().activeColor() : 0x8C2A1717);
        guiGraphics.outline(boxX, boxY, boxSize, boxSize, enabled || hovered ? 0xFFF0D3A3 : 0xB1855C5C);
        if (enabled) {
            guiGraphics.centeredText(this.font, Component.literal("X"), boxX + boxSize / 2, boxY + 2, TEXT_HEADER);
        }
        guiGraphics.text(this.font, label, boxX + boxSize + 7, rect.y() + 4, enabled ? TEXT_PRIMARY : TEXT_MUTED, false);
    }

    private void activateSettingsAction(SettingsActionHitbox hitbox) {
        switch (hitbox.action()) {
            case SET_VISIBILITY -> {
                AllAdvancementsIgtTrackerClient.setCompletionVisibilityMode(this.currentWorldKey(), hitbox.visibilityMode());
                this.refreshData();
            }
            case SET_BACKGROUND -> AllAdvancementsIgtTrackerClient.setMenuBackgroundColor(hitbox.backgroundColor());
            case TOGGLE_NO_FOG -> AllAdvancementsIgtTrackerClient.toggleNoFog();
            case TOGGLE_NO_DARKNESS -> AllAdvancementsIgtTrackerClient.toggleNoDarkness();
            case TOGGLE_FULLBRIGHT -> AllAdvancementsIgtTrackerClient.toggleFullbright();
        }
    }

    private HoveredTile renderGrid(
            GuiGraphicsExtractor guiGraphics,
            int mouseX,
            int mouseY,
            BoardTab activeTab,
            int contentX,
            int gridY,
            int contentWidth,
            int gridHeight,
            GridLayout layout
    ) {
        HoveredTile hoveredTile = null;
        int gridStartX = this.gridStartX(activeTab.kind(), contentX, contentWidth, layout);
        int gridStartY = this.gridStartY(activeTab.kind(), gridY, gridHeight, layout);

        for (int index = 0; index < activeTab.entries().size(); index++) {
            BoardEntry entry = activeTab.entries().get(index);
            int row = index / layout.columns();
            int column = index % layout.columns();
            int tileX = gridStartX + column * (layout.tileWidth() + TILE_GAP);
            int tileY = gridStartY + row * (layout.tileHeight() + TILE_GAP);
            this.renderedTileHitboxes.add(new TileHitbox(entry, tileX, tileY, layout.tileWidth(), layout.tileHeight()));
            HoveredTile candidate = this.renderTile(guiGraphics, mouseX, mouseY, entry, tileX, tileY, layout);
            if (candidate != null) {
                hoveredTile = candidate;
            }
        }

        return hoveredTile;
    }

    private HoveredTile renderTile(
            GuiGraphicsExtractor guiGraphics,
            int mouseX,
            int mouseY,
            BoardEntry entry,
            int tileX,
            int tileY,
            GridLayout layout
    ) {
        boolean hovered = inside(mouseX, mouseY, tileX, tileY, layout.tileWidth(), layout.tileHeight());
        boolean selected = entry.id().equals(this.selectedAdvancementId);
        int background = entry.done()
                ? hovered || selected ? 0xE1B68833 : 0xCC967126
                : hovered || selected ? 0xE1825252 : 0xCC6C3D3D;
        int border = hovered ? 0xFFEED0A0 : selected ? 0xFFF8E3B8 : 0xAA4A2626;

        guiGraphics.fill(tileX, tileY, tileX + layout.tileWidth(), tileY + layout.tileHeight(), background);
        guiGraphics.outline(tileX, tileY, layout.tileWidth(), layout.tileHeight(), border);

        float iconScale = layout.iconScale();
        float iconY = tileY + (layout.tileHeight() - layout.iconSize()) / 2.0F;
        var pose = guiGraphics.pose();
        pose.pushMatrix();
        pose.translate(tileX + 7.0F, iconY);
        pose.scale(iconScale, iconScale);
        guiGraphics.item(entry.icon(), 0, 0);
        pose.popMatrix();

        int textX = tileX + layout.iconSize() + 11;
        int textWidth = Math.max(20, tileX + layout.tileWidth() - textX - 6);
        int textTop = tileY + Math.max(5, (layout.tileHeight() - (TILE_TEXT_LINE_HEIGHT * 2 + TILE_TEXT_GAP)) / 2);

        guiGraphics.text(
                this.font,
                this.ellipsize(entry.title().getString(), textWidth),
                textX,
                textTop,
                entry instanceof AdvancementBoardEntry && entry.done() ? TEXT_COMPLETED_TITLE : TEXT_PRIMARY,
                false
        );

        String subtitle = this.tileSubtitle(entry);
        if (!subtitle.isEmpty()) {
            int subtitleY = textTop + TILE_TEXT_LINE_HEIGHT + TILE_TEXT_GAP;
            int subtitleColor = entry instanceof AdvancementBoardEntry && entry.done()
                    ? TEXT_PRIMARY
                    : entry.done() ? TEXT_SUCCESS : TEXT_SECONDARY;
            this.drawTileSubtitle(guiGraphics, entry, subtitle, hovered, textX, subtitleY, textWidth, subtitleColor);
        }

        return hovered ? new HoveredTile(entry) : null;
    }

    private void renderDetailPanel(
            GuiGraphicsExtractor guiGraphics,
            BoardEntry entry,
            int x,
            int y,
            int width,
            int height,
            int mouseX,
            int mouseY
    ) {
        this.renderedRequirementViewport = null;
        guiGraphics.fill(x, y, x + width, y + height, this.menuBackgroundColor().panelColor());
        guiGraphics.outline(x, y, width, height, 0xC0906969);

        if (entry == null) {
            BoardTab activeTab = this.getActiveTab();
            if (activeTab != null && activeTab.kind() == TabKind.WAYPOINTS) {
                this.renderWaypointDetailPanel(guiGraphics, null, x, y, width, height, mouseX, mouseY);
                return;
            }
            this.requirementScrollAdvancementId = null;
            this.requirementScrollOffset = 0;
            guiGraphics.centeredText(
                    this.font,
                    Component.translatable("screen.aaigttracker.hover_hint"),
                    x + width / 2,
                    y + height / 2 - 4,
                    TEXT_SECONDARY
            );
            return;
        }

        if (entry instanceof WaypointBoardEntry waypointEntry) {
            this.renderWaypointDetailPanel(guiGraphics, waypointEntry.snapshot(), x, y, width, height, mouseX, mouseY);
            return;
        }

        if (entry instanceof ItemBoardEntry itemEntry) {
            this.renderItemDetailPanel(guiGraphics, itemEntry.snapshot(), x, y, width, height, mouseX, mouseY);
            return;
        }

        AdvancementSnapshot snapshot = ((AdvancementBoardEntry) entry).snapshot();
        boolean noTrackedRequirements = TrackerDataRepository.current().advancements().detaillessAdvancements().contains(snapshot.id());
        if (noTrackedRequirements) {
            this.requirementScrollAdvancementId = null;
            this.requirementScrollOffset = 0;
            guiGraphics.centeredText(
                    this.font,
                    Component.translatable("screen.aaigttracker.requirements.none_tracked"),
                    x + width / 2,
                    y + height / 2 - 4,
                    TEXT_MUTED
            );
            return;
        }

        if (snapshot.done()) {
            this.requirementScrollAdvancementId = null;
            this.requirementScrollOffset = 0;
            this.renderCompletedAdvancementDetail(guiGraphics, snapshot, x, y, width);
            return;
        }

        if (snapshot.id().equals(Identifier.withDefaultNamespace("husbandry/froglights"))) {
            this.renderFroglightRequirements(guiGraphics, snapshot, x, y, width, height, mouseX, mouseY);
            return;
        }

        RequirementMode requirementMode = this.requirementMode(snapshot);
        if (requirementMode == RequirementMode.SINGLE) {
            this.requirementScrollAdvancementId = null;
            this.requirementScrollOffset = 0;
            guiGraphics.centeredText(
                    this.font,
                    Component.translatable("screen.aaigttracker.requirements.single"),
                    x + width / 2,
                    y + height / 2 - 4,
                    TEXT_MUTED
            );
            return;
        }

        Component summary = this.requirementSummary(requirementMode);
        int headerX = x + DETAIL_INNER_PADDING;
        int headerY = y + DETAIL_INNER_PADDING;
        guiGraphics.text(this.font, summary, headerX, headerY, TEXT_COUNT, false);
        if (requirementMode != RequirementMode.ONE_OF) {
            Component counter = Component.literal(snapshot.completedCriteria() + " / " + snapshot.totalCriteria());
            guiGraphics.text(
                    this.font,
                    counter,
                    x + width - DETAIL_INNER_PADDING - this.font.width(counter),
                    headerY,
                    TEXT_MUTED,
                    false
            );
        }

        int listY = headerY + REQUIREMENT_HEADER_HEIGHT + REQUIREMENT_HEADER_GAP;
        int listHeight = y + height - DETAIL_INNER_PADDING - listY;
        if (listHeight <= 0) {
            this.requirementScrollAdvancementId = null;
            this.requirementScrollOffset = 0;
            return;
        }

        this.renderRequirementSections(
                guiGraphics,
                snapshot.id(),
                this.buildRequirementSections(snapshot, requirementMode),
                x + DETAIL_INNER_PADDING,
                listY,
                width - DETAIL_INNER_PADDING * 2,
                listHeight,
                mouseX,
                mouseY
        );
    }

    private void renderFroglightRequirements(
            GuiGraphicsExtractor guiGraphics,
            AdvancementSnapshot snapshot,
            int x,
            int y,
            int width,
            int height,
            int mouseX,
            int mouseY
    ) {
        int headerY = y + DETAIL_INNER_PADDING;
        guiGraphics.text(this.font, Component.translatable("screen.aaigttracker.requirements.all_of"),
                x + DETAIL_INNER_PADDING, headerY, TEXT_COUNT, false);
        int listY = headerY + REQUIREMENT_HEADER_HEIGHT + REQUIREMENT_HEADER_GAP;
        // Vanilla awards one criterion for having all three lights at once. These
        // rows explain the frog/light mapping, without inventing per-light progress.
        List<CriterionSnapshot> frogs = List.of(
                froglightRequirement("temperate", Items.OCHRE_FROGLIGHT),
                froglightRequirement("warm", Items.PEARLESCENT_FROGLIGHT),
                froglightRequirement("cold", Items.VERDANT_FROGLIGHT)
        );
        this.renderRequirementSections(guiGraphics, snapshot.id(), List.of(new RequirementSection(null, frogs)),
                x + DETAIL_INNER_PADDING, listY, width - DETAIL_INNER_PADDING * 2,
                y + height - DETAIL_INNER_PADDING - listY, mouseX, mouseY);
    }

    private static CriterionSnapshot froglightRequirement(String variant, Item froglight) {
        return new CriterionSnapshot(
                variant,
                Component.translatable("screen.aaigttracker.froglight." + variant),
                CriterionIcon.entity(froglight.getDefaultInstance(), EntityPreviewSpec.frog(Identifier.withDefaultNamespace(variant))),
                List.of(CriterionIcon.item(froglight.getDefaultInstance())),
                SupplementaryIconMode.STATIC_ALL,
                false,
                null,
                false
        );
    }

    private void renderItemDetailPanel(
            GuiGraphicsExtractor guiGraphics,
            ItemProgressSnapshot snapshot,
            int x,
            int y,
            int width,
            int height,
            int mouseX,
            int mouseY
    ) {
        int headerX = x + DETAIL_INNER_PADDING;
        int headerY = y + DETAIL_INNER_PADDING;
        Component counter = Component.literal(snapshot.current() + " / " + snapshot.total());
        int counterWidth = this.font.width(counter);
        int headerWidth = Math.max(16, width - DETAIL_INNER_PADDING * 2 - counterWidth - 8);
        guiGraphics.text(
                this.font,
                this.ellipsize(snapshot.description().getString(), headerWidth),
                headerX,
                headerY,
                TEXT_COUNT,
                false
        );
        guiGraphics.text(
                this.font,
                counter,
                x + width - DETAIL_INNER_PADDING - counterWidth,
                headerY,
                TEXT_MUTED,
                false
        );

        int listY = headerY + REQUIREMENT_HEADER_HEIGHT + REQUIREMENT_HEADER_GAP;
        int listHeight = y + height - DETAIL_INNER_PADDING - listY;
        if (listHeight <= 0) {
            this.requirementScrollAdvancementId = null;
            this.requirementScrollOffset = 0;
            return;
        }

        List<CriterionSnapshot> visibleCriteria = this.visibleRequirementCriteria(snapshot.criteria());
        if (visibleCriteria.isEmpty()) {
            this.requirementScrollAdvancementId = null;
            this.requirementScrollOffset = 0;
            guiGraphics.centeredText(
                    this.font,
                    Component.translatable("screen.aaigttracker.filter.empty"),
                    x + width / 2,
                    y + height / 2 - 4,
                    TEXT_MUTED
            );
            return;
        }

        this.renderRequirementSections(
                guiGraphics,
                snapshot.id(),
                List.of(new RequirementSection(null, visibleCriteria)),
                x + DETAIL_INNER_PADDING,
                listY,
                width - DETAIL_INNER_PADDING * 2,
                listHeight,
                mouseX,
                mouseY
        );
    }

    private void renderCompletedAdvancementDetail(
            GuiGraphicsExtractor guiGraphics,
            AdvancementSnapshot snapshot,
            int x,
            int y,
            int width
    ) {
        int textX = x + DETAIL_INNER_PADDING;
        int textWidth = Math.max(16, width - DETAIL_INNER_PADDING * 2);
        int cursorY = y + DETAIL_INNER_PADDING;

        guiGraphics.text(
                this.font,
                Component.translatable("screen.aaigttracker.requirements.complete"),
                textX,
                cursorY,
                TEXT_SUCCESS,
                false
        );
        cursorY += 16;

        Component time = snapshot.hasKnownCompletionTime()
                ? Component.translatable("screen.aaigttracker.completion.time", formatMillis(snapshot.completionTimeMillis()))
                : Component.translatable("screen.aaigttracker.completion.time_unknown");
        guiGraphics.text(
                this.font,
                this.ellipsize(time.getString(), textWidth),
                textX,
                cursorY,
                snapshot.hasKnownCompletionTime() ? TEXT_PRIMARY : TEXT_MUTED,
                false
        );
        cursorY += 12;

        Component location = snapshot.completionLocation() != null
                ? Component.translatable("screen.aaigttracker.completion.location", snapshot.completionLocation().compactText())
                : Component.translatable("screen.aaigttracker.completion.location_unknown");
        guiGraphics.text(
                this.font,
                this.ellipsize(location.getString(), textWidth),
                textX,
                cursorY,
                snapshot.completionLocation() != null ? TEXT_PRIMARY : TEXT_MUTED,
                false
        );
    }

    private void renderWaypointDetailPanel(
            GuiGraphicsExtractor guiGraphics,
            WaypointSnapshot waypoint,
            int x,
            int y,
            int width,
            int height,
            int mouseX,
            int mouseY
    ) {
        this.requirementScrollAdvancementId = null;
        this.requirementScrollOffset = 0;
        this.renderedRequirementViewport = null;

        int headerX = x + DETAIL_INNER_PADDING;
        int headerY = y + DETAIL_INNER_PADDING;
        Component title = waypoint == null
                ? Component.translatable("screen.aaigttracker.waypoints.empty")
                : waypoint.title();
        guiGraphics.text(this.font, title, headerX, headerY, TEXT_COUNT, false);

        if (waypoint != null) {
            guiGraphics.text(
                    this.font,
                    Component.literal(waypoint.positionText()),
                    headerX,
                    headerY + 14,
                    TEXT_SECONDARY,
                    false
            );
        } else {
            guiGraphics.text(
                    this.font,
                    Component.translatable("screen.aaigttracker.waypoints.empty_hint"),
                    headerX,
                    headerY + 14,
                    TEXT_SECONDARY,
                    false
            );
        }

        int buttonY = y + height - DETAIL_INNER_PADDING - 20;
        int buttonWidth = 118;
        int gap = 8;
        int cursorX = headerX;
        Rect addRect = new Rect(cursorX, buttonY, buttonWidth, 20);
        this.renderWaypointButton(guiGraphics, addRect, Component.translatable("screen.aaigttracker.waypoints.add"), addRect.contains(mouseX, mouseY));
        this.renderedWaypointActionHitboxes.add(new WaypointActionHitbox(WaypointAction.ADD, null, addRect));
        cursorX += buttonWidth + gap;

        if (waypoint != null) {
            Rect renameRect = new Rect(cursorX, buttonY, buttonWidth, 20);
            this.renderWaypointButton(guiGraphics, renameRect, Component.translatable("screen.aaigttracker.waypoints.rename"), renameRect.contains(mouseX, mouseY));
            this.renderedWaypointActionHitboxes.add(new WaypointActionHitbox(WaypointAction.RENAME, waypoint.id(), renameRect));
            cursorX += buttonWidth + gap;

            Rect deleteRect = new Rect(cursorX, buttonY, buttonWidth, 20);
            this.renderWaypointButton(guiGraphics, deleteRect, Component.translatable("screen.aaigttracker.waypoints.delete"), deleteRect.contains(mouseX, mouseY));
            this.renderedWaypointActionHitboxes.add(new WaypointActionHitbox(WaypointAction.DELETE, waypoint.id(), deleteRect));
        }
    }

    private void renderWaypointButton(GuiGraphicsExtractor guiGraphics, Rect rect, Component label, boolean hovered) {
        guiGraphics.fill(rect.x(), rect.y(), rect.right(), rect.bottom(), hovered ? 0xD97A4A3D : 0xB95F3434);
        if (hovered) {
            guiGraphics.fill(rect.x() + 1, rect.y() + 1, rect.right() - 1, rect.bottom() - 1, 0x24FFE6B8);
        }
        guiGraphics.outline(rect.x(), rect.y(), rect.width(), rect.height(), hovered ? 0xFFFFE6B8 : 0xD4B48A8A);
        guiGraphics.centeredText(
                this.font,
                Component.literal(this.ellipsize(label.getString(), rect.width() - 8)),
                rect.x() + rect.width() / 2,
                rect.y() + 6,
                hovered ? TEXT_HEADER : TEXT_PRIMARY
        );
    }

    private void renderRequirementSections(
            GuiGraphicsExtractor guiGraphics,
            Identifier entryId,
            List<RequirementSection> sections,
            int x,
            int y,
            int width,
            int height,
            int mouseX,
            int mouseY
    ) {
        if (sections.isEmpty() || height <= 0 || width <= 0) {
            this.requirementScrollAdvancementId = null;
            this.requirementScrollOffset = 0;
            return;
        }

        RequirementRenderData renderData = this.createRequirementRenderData(sections, width, height);
        this.prepareRequirementScroll(entryId, renderData.overflowHeight());
        this.renderedRequirementViewport = new RequirementViewport(x, y, width, height, renderData.overflowHeight());

        guiGraphics.enableScissor(x, y, x + renderData.contentWidth(), y + height);
        int cursorY = y - this.requirementScrollOffset;
        for (int index = 0; index < renderData.layouts().size(); index++) {
            RequirementSectionLayout layout = renderData.layouts().get(index);
            this.renderRequirementSection(guiGraphics, layout, x, cursorY, mouseX, mouseY);
            cursorY += layout.height();
            if (index + 1 < renderData.layouts().size()) {
                cursorY += REQUIREMENT_SECTION_GAP;
            }
        }
        guiGraphics.disableScissor();

        if (renderData.overflowHeight() > 0) {
            this.renderRequirementScrollbar(
                    guiGraphics,
                    x + width - REQUIREMENT_SCROLLBAR_WIDTH,
                    y,
                    REQUIREMENT_SCROLLBAR_WIDTH,
                    height,
                    renderData.contentHeight(),
                    this.requirementScrollOffset
            );
        }
    }

    private void renderRequirementSection(
            GuiGraphicsExtractor guiGraphics,
            RequirementSectionLayout layout,
            int x,
            int y,
            int mouseX,
            int mouseY
    ) {
        int contentY = y;
        if (layout.section().label() != null) {
            guiGraphics.text(this.font, layout.section().label(), x, y, TEXT_MUTED, false);
            contentY += REQUIREMENT_HEADER_HEIGHT + REQUIREMENT_GROUP_LABEL_GAP;
        }

        int rowStep = REQUIREMENT_ENTRY_HEIGHT + REQUIREMENT_ENTRY_GAP;
        for (int index = 0; index < layout.section().criteria().size(); index++) {
            int column = index % layout.columns();
            int row = index / layout.columns();
            int lineX = x + column * (layout.columnWidth() + REQUIREMENT_COLUMN_GAP);
            int lineY = contentY + row * rowStep;
            // Scissoring only clips drawing; skip hidden rows before loading textures or building previews.
            if (this.renderedRequirementViewport != null) {
                if (lineY + REQUIREMENT_ENTRY_HEIGHT <= this.renderedRequirementViewport.y()) {
                    continue;
                }
                if (lineY >= this.renderedRequirementViewport.y() + this.renderedRequirementViewport.height()) {
                    break;
                }
            }
            CriterionSnapshot criterion = layout.section().criteria().get(index);
            boolean hovered = this.renderedRequirementViewport != null
                    && this.renderedRequirementViewport.contains(mouseX, mouseY)
                    && inside(mouseX, mouseY, lineX, lineY, layout.columnWidth(), REQUIREMENT_ENTRY_HEIGHT);
            int background = criterion.completed()
                    ? (hovered ? 0xBC416A36 : 0xA0355A2E)
                    : (hovered ? 0x945F3434 : 0x7D4F2B2B);
            int border = hovered
                    ? (this.isPreviewable(criterion) ? 0xFFF0D3A3 : 0xD4B48A8A)
                    : (criterion.completed() ? 0xD46EBD72 : 0xB18C6767);
            int textColor = criterion.completed() ? TEXT_SUCCESS : TEXT_BODY;
            guiGraphics.fill(lineX, lineY, lineX + layout.columnWidth(), lineY + REQUIREMENT_ENTRY_HEIGHT, background);
            guiGraphics.outline(lineX, lineY, layout.columnWidth(), REQUIREMENT_ENTRY_HEIGHT, border);

            int textX = lineX + 6;
            int primaryIconX = 0;
            int primaryIconWidth = 0;
            if (!criterion.icon().isEmpty()) {
                primaryIconX = lineX + 3;
                primaryIconWidth = this.primaryIconSlotWidth(criterion.icon());
                this.renderCriterionIcon(guiGraphics, criterion.icon(), primaryIconX, lineY + 1, primaryIconWidth, REQUIREMENT_ICON_SIZE);
                textX = primaryIconX + primaryIconWidth + REQUIREMENT_ICON_GAP;
            }

            if (criterion.supplementaryIconMode() == SupplementaryIconMode.STATIC_ALL) {
                for (CriterionIcon supplementaryIcon : criterion.supplementaryIcons()) {
                    if (supplementaryIcon == null || supplementaryIcon.isEmpty()) {
                        continue;
                    }
                    this.renderCriterionIcon(guiGraphics, supplementaryIcon, textX, lineY + 1, REQUIREMENT_ICON_SIZE, REQUIREMENT_ICON_SIZE);
                    textX += REQUIREMENT_ICON_SIZE + REQUIREMENT_ICON_GAP;
                }
            } else {
                CriterionIcon supplementaryIcon = this.currentSupplementaryIcon(criterion);
                if (supplementaryIcon != null && !supplementaryIcon.isEmpty()) {
                    this.renderCriterionIcon(guiGraphics, supplementaryIcon, textX, lineY + 1, REQUIREMENT_ICON_SIZE, REQUIREMENT_ICON_SIZE);
                    textX += REQUIREMENT_ICON_SIZE + REQUIREMENT_ICON_GAP;
                }
            }

            int textWidth = Math.max(12, layout.columnWidth() - (textX - lineX) - 6);
            guiGraphics.text(
                    this.font,
                    this.ellipsize(criterion.displayName().getString(), textWidth),
                    textX,
                    lineY + 5,
                    textColor,
                    false
            );
            this.renderedRequirementHitboxes.add(
                    new RequirementHitbox(
                            criterion,
                            lineX,
                            lineY,
                            layout.columnWidth(),
                            REQUIREMENT_ENTRY_HEIGHT,
                            primaryIconX,
                            lineY + 1,
                            primaryIconWidth,
                            primaryIconWidth > 0 ? REQUIREMENT_ICON_SIZE : 0
                    )
            );
        }
    }

    private List<RequirementSection> buildRequirementSections(AdvancementSnapshot snapshot, RequirementMode requirementMode) {
        if (requirementMode == RequirementMode.ALL_OF) {
            List<CriterionSnapshot> visibleCriteria = this.visibleRequirementCriteria(snapshot.criteria());
            return visibleCriteria.isEmpty()
                    ? List.of()
                    : List.of(new RequirementSection(null, visibleCriteria));
        }

        if (requirementMode == RequirementMode.ONE_OF) {
            if (snapshot.requirementGroups().isEmpty()) {
                return List.of();
            }

            List<CriterionSnapshot> visibleCriteria = this.visibleRequirementCriteria(
                    snapshot.requirementGroups().get(0).criteria()
            );
            return visibleCriteria.isEmpty()
                    ? List.of()
                    : List.of(new RequirementSection(null, visibleCriteria));
        }

        List<RequirementSection> sections = new ArrayList<>(snapshot.requirementGroups().size());
        for (RequirementGroupSnapshot group : snapshot.requirementGroups()) {
            List<CriterionSnapshot> visibleCriteria = this.visibleRequirementCriteria(group.criteria());
            if (visibleCriteria.isEmpty()) {
                continue;
            }

            Component label = group.criteria().size() > 1
                    ? Component.translatable("screen.aaigttracker.requirements.one_of_group")
                    : Component.translatable("screen.aaigttracker.requirements.required_group");
            sections.add(new RequirementSection(label, visibleCriteria));
        }
        return List.copyOf(sections);
    }

    private List<CriterionSnapshot> visibleRequirementCriteria(List<CriterionSnapshot> criteria) {
        if (!this.visibilityMode().hidesCompletedCriteria()) {
            return criteria;
        }

        List<CriterionSnapshot> visibleCriteria = new ArrayList<>(criteria.size());
        for (CriterionSnapshot criterion : criteria) {
            if (!criterion.completed()) {
                visibleCriteria.add(criterion);
            }
        }
        return List.copyOf(visibleCriteria);
    }

    private RequirementSectionLayout createRequirementSectionLayout(RequirementSection section, int width) {
        int columns = this.requirementColumnCount(section.criteria().size(), width);
        int columnWidth = (width - Math.max(0, columns - 1) * REQUIREMENT_COLUMN_GAP) / columns;
        int rows = Math.max(1, (section.criteria().size() + columns - 1) / columns);
        int contentHeight = rows * (REQUIREMENT_ENTRY_HEIGHT + REQUIREMENT_ENTRY_GAP) - REQUIREMENT_ENTRY_GAP;
        int height = contentHeight;
        if (section.label() != null) {
            height += REQUIREMENT_HEADER_HEIGHT + REQUIREMENT_GROUP_LABEL_GAP;
        }

        return new RequirementSectionLayout(section, columns, rows, columnWidth, height);
    }

    private RequirementRenderData createRequirementRenderData(List<RequirementSection> sections, int width, int height) {
        List<RequirementSectionLayout> layouts = this.createRequirementLayouts(sections, width);
        int contentHeight = this.requirementContentHeight(layouts);
        if (contentHeight <= height) {
            return new RequirementRenderData(List.copyOf(layouts), width, contentHeight, 0);
        }

        int contentWidth = Math.max(1, width - REQUIREMENT_SCROLLBAR_WIDTH - REQUIREMENT_SCROLLBAR_GAP);
        layouts = this.createRequirementLayouts(sections, contentWidth);
        contentHeight = this.requirementContentHeight(layouts);
        return new RequirementRenderData(
                List.copyOf(layouts),
                contentWidth,
                contentHeight,
                Math.max(0, contentHeight - height)
        );
    }

    private List<RequirementSectionLayout> createRequirementLayouts(List<RequirementSection> sections, int width) {
        List<RequirementSectionLayout> layouts = new ArrayList<>(sections.size());
        for (RequirementSection section : sections) {
            layouts.add(this.createRequirementSectionLayout(section, width));
        }
        return layouts;
    }

    private int requirementContentHeight(List<RequirementSectionLayout> layouts) {
        int contentHeight = 0;
        for (RequirementSectionLayout layout : layouts) {
            contentHeight += layout.height();
        }
        if (!layouts.isEmpty()) {
            contentHeight += Math.max(0, layouts.size() - 1) * REQUIREMENT_SECTION_GAP;
        }
        return contentHeight;
    }

    private int requirementColumnCount(int itemCount, int width) {
        if (itemCount <= 1 || width <= MIN_REQUIREMENT_COLUMN_WIDTH) {
            return 1;
        }

        return Math.max(1, Math.min(itemCount, (width + REQUIREMENT_COLUMN_GAP) / (MIN_REQUIREMENT_COLUMN_WIDTH + REQUIREMENT_COLUMN_GAP)));
    }

    private RequirementMode requirementMode(AdvancementSnapshot snapshot) {
        if (snapshot.totalCriteria() <= 1 || snapshot.requirementGroups().isEmpty()) {
            return RequirementMode.SINGLE;
        }

        boolean hasMultiOptionGroup = snapshot.requirementGroups().stream().anyMatch(group -> group.criteria().size() > 1);
        if (!hasMultiOptionGroup) {
            return RequirementMode.ALL_OF;
        }

        if (snapshot.requirementGroups().size() == 1) {
            return RequirementMode.ONE_OF;
        }

        return RequirementMode.GROUPED;
    }

    private Component requirementSummary(RequirementMode requirementMode) {
        return switch (requirementMode) {
            case ONE_OF -> Component.translatable("screen.aaigttracker.requirements.one_of");
            case ALL_OF -> Component.translatable("screen.aaigttracker.requirements.all_of");
            case GROUPED -> Component.translatable("screen.aaigttracker.requirements.grouped");
            case SINGLE -> Component.empty();
        };
    }

    private CompletionVisibilityMode visibilityMode() {
        return AllAdvancementsIgtTrackerClient.getCompletionVisibilityMode(this.currentWorldKey());
    }

    private MenuBackgroundColor menuBackgroundColor() {
        return AllAdvancementsIgtTrackerClient.getMenuBackgroundColor();
    }

    private void rememberCurrentState() {
        AllAdvancementsIgtTrackerClient.rememberTrackerState(
                this.currentWorldKey(),
                this.visibilityMode(),
                this.activeRootId,
                this.selectedAdvancementId
        );
    }

    private WorldKey currentWorldKey() {
        return this.trackerManager.getCurrentWorldKey();
    }

    private void hideDebugOverlay() {
        if (this.debugOverlayWasVisible != null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            this.debugOverlayWasVisible = Boolean.FALSE;
            return;
        }

        this.debugOverlayWasVisible = minecraft.debugEntries.isOverlayVisible();
        if (minecraft.debugEntries.isOverlayVisible()) {
            minecraft.debugEntries.setOverlayVisible(false);
        }
    }

    private void restoreDebugOverlay() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && Boolean.TRUE.equals(this.debugOverlayWasVisible)) {
            minecraft.debugEntries.setOverlayVisible(true);
        }
    }

    private void cycleTab(int offset) {
        if (this.cachedTabs.isEmpty()) {
            return;
        }

        int currentIndex = this.activeTabIndex();
        int nextIndex = Math.floorMod(currentIndex + offset, this.cachedTabs.size());
        this.selectTab(nextIndex);
    }

    private void selectTab(int index) {
        if (index < 0 || index >= this.cachedTabs.size()) {
            return;
        }

        this.activeRootId = this.cachedTabs.get(index).rootId();
        this.selectedAdvancementId = null;
        if (ITEMS_TAB_ID.equals(this.activeRootId)) {
            this.nextItemScanAt = 0L;
            this.refreshItemEntries(true);
        }
        if (WAYPOINTS_TAB_ID.equals(this.activeRootId)) {
            this.cachedWaypoints = this.waypointService.waypoints(this.currentWorldKey());
        }
        this.rememberCurrentState();
    }

    private int activeTabIndex() {
        for (int index = 0; index < this.cachedTabs.size(); index++) {
            if (this.cachedTabs.get(index).rootId().equals(this.activeRootId)) {
                return index;
            }
        }

        return 0;
    }

    private BoardTab getActiveTab() {
        if (this.activeRootId == null) {
            return this.cachedTabs.isEmpty() ? null : this.cachedTabs.get(0);
        }

        for (BoardTab tab : this.cachedTabs) {
            if (tab.rootId().equals(this.activeRootId)) {
                return tab;
            }
        }

        return this.cachedTabs.isEmpty() ? null : this.cachedTabs.get(0);
    }

    private BoardEntry getSelectedEntry(BoardTab activeTab) {
        if (activeTab == null || this.selectedAdvancementId == null) {
            return null;
        }

        for (BoardEntry entry : activeTab.entries()) {
            if (entry.id().equals(this.selectedAdvancementId)) {
                return entry;
            }
        }

        return null;
    }

    private int gridHeight(int gridY, int contentBottom, int detailHeight) {
        int reserved = detailHeight > 0 ? detailHeight + CONTENT_GAP : 0;
        return Math.max(1, contentBottom - gridY - reserved);
    }

    private int detailPanelHeight(BoardEntry entry) {
        return DETAIL_HEIGHT;
    }

    private BoardEntry findHoveredEntry(
            BoardTab activeTab,
            int mouseX,
            int mouseY,
            int contentX,
            int gridY,
            int contentWidth,
            int gridHeight,
            GridLayout layout
    ) {
        int gridStartX = this.gridStartX(activeTab.kind(), contentX, contentWidth, layout);
        int gridStartY = this.gridStartY(activeTab.kind(), gridY, gridHeight, layout);
        for (int index = 0; index < activeTab.entries().size(); index++) {
            BoardEntry entry = activeTab.entries().get(index);
            int row = index / layout.columns();
            int column = index % layout.columns();
            int tileX = gridStartX + column * (layout.tileWidth() + TILE_GAP);
            int tileY = gridStartY + row * (layout.tileHeight() + TILE_GAP);
            if (inside(mouseX, mouseY, tileX, tileY, layout.tileWidth(), layout.tileHeight())) {
                return entry;
            }
        }

        return null;
    }

    private int gridStartX(TabKind kind, int contentX, int contentWidth, GridLayout layout) {
        if (kind == TabKind.WAYPOINTS) {
            return contentX;
        }

        return contentX + Math.max(0, (contentWidth - layout.totalWidth()) / 2);
    }

    private int gridStartY(TabKind kind, int gridY, int gridHeight, GridLayout layout) {
        if (kind == TabKind.WAYPOINTS) {
            return gridY;
        }

        return gridY + Math.max(0, (gridHeight - layout.totalHeight()) / 2);
    }

    private GridLayout computeGridLayout(int entryCount, int availableWidth, int availableHeight) {
        if (entryCount <= 0) {
            return this.createGridLayout(1, 1, availableWidth, availableHeight);
        }

        int widthLimitedColumns = Math.max(
                1,
                Math.min(entryCount, (availableWidth + TILE_GAP) / (EMERGENCY_MIN_TILE_WIDTH + TILE_GAP))
        );

        for (int columns = widthLimitedColumns; columns >= 1; columns--) {
            int rows = (entryCount + columns - 1) / columns;
            int rawTileWidth = (availableWidth - Math.max(0, columns - 1) * TILE_GAP) / columns;
            int rawTileHeight = (availableHeight - Math.max(0, rows - 1) * TILE_GAP) / rows;
            if (rawTileWidth >= MIN_TILE_WIDTH && rawTileHeight >= MIN_TILE_HEIGHT) {
                return this.createGridLayout(columns, rows, rawTileWidth, rawTileHeight);
            }
        }

        for (int columns = widthLimitedColumns; columns >= 1; columns--) {
            int rows = (entryCount + columns - 1) / columns;
            int rawTileWidth = (availableWidth - Math.max(0, columns - 1) * TILE_GAP) / columns;
            int rawTileHeight = (availableHeight - Math.max(0, rows - 1) * TILE_GAP) / rows;
            if (rawTileWidth > 0 && rawTileHeight > 0) {
                return this.createGridLayout(columns, rows, rawTileWidth, rawTileHeight);
            }
        }

        return this.createGridLayout(1, entryCount, availableWidth, Math.max(1, availableHeight / entryCount));
    }

    private GridLayout createGridLayout(int columns, int rows, int rawTileWidth, int rawTileHeight) {
        int tileWidth = Math.max(1, Math.min(MAX_TILE_WIDTH, rawTileWidth));
        int tileHeight = Math.max(1, Math.min(MAX_TILE_HEIGHT, rawTileHeight));
        int iconSize = Math.max(10, Math.min(18, tileHeight - 14));
        return new GridLayout(columns, rows, tileWidth, tileHeight, iconSize / 16.0F, iconSize);
    }

    private String tileSubtitle(BoardEntry entry) {
        if (entry instanceof AdvancementBoardEntry advancementEntry) {
            AdvancementSnapshot snapshot = advancementEntry.snapshot();
            if (snapshot.done()) {
                return snapshot.hasKnownCompletionTime()
                        ? Component.translatable("screen.aaigttracker.completion.time", formatMillis(snapshot.completionTimeMillis())).getString()
                        : Component.translatable("screen.aaigttracker.completion.time_unknown").getString();
            }

            return this.singleLineText(snapshot.description().getString());
        }

        if (entry instanceof ItemBoardEntry itemEntry) {
            ItemProgressSnapshot snapshot = itemEntry.snapshot();
            return snapshot.current() + "/" + snapshot.total();
        }

        if (entry instanceof WaypointBoardEntry waypointEntry) {
            return waypointEntry.snapshot().positionText();
        }

        return "";
    }

    private void drawTileSubtitle(
            GuiGraphicsExtractor guiGraphics,
            BoardEntry entry,
            String subtitle,
            boolean hovered,
            int x,
            int y,
            int width,
            int color
    ) {
        if (!(entry instanceof AdvancementBoardEntry advancementEntry)) {
            guiGraphics.text(this.font, this.ellipsize(subtitle, width), x, y, color, false);
            return;
        }

        if (hovered) {
            this.updateHoveredSubtitleState(advancementEntry.snapshot().id());
        }

        int renderedWidth = this.font.width(subtitle);
        if (!hovered || renderedWidth <= width) {
            guiGraphics.text(this.font, this.ellipsize(subtitle, width), x, y, color, false);
            return;
        }

        int cycleWidth = renderedWidth + MARQUEE_LOOP_GAP;
        int offset = this.computeMarqueeOffset(cycleWidth);
        guiGraphics.enableScissor(x, y, x + width, y + TILE_SUBTITLE_SCISSOR_HEIGHT);
        guiGraphics.text(this.font, subtitle, x - offset, y, color, false);
        guiGraphics.text(this.font, subtitle, x - offset + cycleWidth, y, color, false);
        guiGraphics.disableScissor();
    }

    private void updateHoveredSubtitleState(Identifier hoveredId) {
        if (!Objects.equals(this.hoveredSubtitleAdvancementId, hoveredId)) {
            this.hoveredSubtitleAdvancementId = hoveredId;
            this.hoveredSubtitleStartedAt = this.currentRenderMillis;
        }
    }

    private int computeMarqueeOffset(int cycleWidth) {
        if (cycleWidth <= 0) {
            return 0;
        }

        long elapsedMillis = Math.max(0L, this.currentRenderMillis - this.hoveredSubtitleStartedAt);
        return Math.floorMod((int) Math.round(elapsedMillis * MARQUEE_PIXELS_PER_MILLISECOND), cycleWidth);
    }

    private void prepareRequirementScroll(Identifier advancementId, int maxScroll) {
        if (!Objects.equals(this.requirementScrollAdvancementId, advancementId)) {
            this.requirementScrollAdvancementId = advancementId;
            this.requirementScrollOffset = 0;
        }

        this.requirementScrollOffset = clamp(this.requirementScrollOffset, 0, maxScroll);
    }

    private void renderRequirementTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        for (RequirementHitbox hitbox : this.renderedRequirementHitboxes) {
            if (!hitbox.criterion().showCompletionTooltip()
                    || !hitbox.criterion().completed()
                    || !hitbox.contains(mouseX, mouseY)) {
                continue;
            }

            List<Component> tooltip = new ArrayList<>(2);
            tooltip.add(hitbox.criterion().displayName());
            tooltip.add(hitbox.criterion().hasKnownCompletionTime()
                    ? Component.translatable(
                            "screen.aaigttracker.requirement.finished_at",
                            formatMillis(hitbox.criterion().completionTimeMillis())
                    )
                    : Component.translatable("screen.aaigttracker.requirement.finished_unknown"));
            guiGraphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
            return;
        }
    }

    private void renderCriterionIcon(GuiGraphicsExtractor guiGraphics, CriterionIcon icon, int x, int y, int slotWidth, int slotHeight) {
        if (icon == null || icon.isEmpty()) {
            return;
        }

        if (icon.isBiomeSprite()) {
            if (this.renderBiomeSprite(guiGraphics, icon.biomeSpriteSpec(), x, y, slotWidth, slotHeight)) {
                return;
            }
            // Keep biome icons static; use the fallback item while the sprite loads or is missing.
        }

        if (icon.isEntityPreview()) {
            LivingEntity entity = this.previewEntity(icon.entityPreviewSpec());
            if (entity != null) {
                this.renderEntityPortrait(guiGraphics, entity, x, y, slotWidth, slotHeight, false);
                return;
            }
        }

        if (icon.isItem()) {
            int itemX = x + Math.max(0, (slotWidth - REQUIREMENT_ICON_SIZE) / 2);
            int itemY = y + Math.max(0, (slotHeight - REQUIREMENT_ICON_SIZE) / 2);
            guiGraphics.item(icon.copyItemStack(), itemX, itemY);
        }
    }

    private CriterionIcon currentSupplementaryIcon(CriterionSnapshot criterion) {
        if (criterion == null || criterion.supplementaryIcons().isEmpty()) {
            return null;
        }

        if (criterion.supplementaryIconMode() != SupplementaryIconMode.CYCLE_ONE) {
            return criterion.supplementaryIcons().get(0);
        }

        int index = criterion.supplementaryIcons().size() == 1
                ? 0
                : (int) ((this.currentRenderMillis / BREEDING_ICON_CYCLE_MILLIS) % criterion.supplementaryIcons().size());
        return criterion.supplementaryIcons().get(index);
    }

    private int primaryIconSlotWidth(CriterionIcon icon) {
        if (icon != null && icon.isBiomeSprite()) {
            return REQUIREMENT_BIOME_SLOT_WIDTH;
        }
        if (icon == null || !icon.isEntityPreview()) {
            return REQUIREMENT_ICON_SIZE;
        }

        return REQUIREMENT_ENTITY_SLOT_WIDTH;
    }

    private boolean openPreview(CriterionSnapshot criterion) {
        if (!this.isPreviewable(criterion)) {
            return false;
        }

        List<CriterionSnapshot> previewableCriteria = new ArrayList<>();
        int selectedIndex = 0;
        for (RequirementHitbox hitbox : this.renderedRequirementHitboxes) {
            CriterionSnapshot candidate = hitbox.criterion();
            if (!this.isPreviewable(candidate)) {
                continue;
            }
            if (candidate == criterion) {
                selectedIndex = previewableCriteria.size();
            }
            previewableCriteria.add(candidate);
        }
        if (previewableCriteria.isEmpty()) {
            previewableCriteria = List.of(criterion);
        }

        this.previewOverlay = new PreviewOverlay(previewableCriteria, selectedIndex);
        return true;
    }

    private boolean isPreviewable(CriterionSnapshot criterion) {
        if (criterion == null || criterion.icon() == null || criterion.icon().isEmpty()) {
            return false;
        }

        if (criterion.icon().isBiomeSprite()) {
            return false;
        }

        Component subtitle = this.previewSubtitle(criterion);
        boolean entityPreview = criterion.icon().isEntityPreview();
        boolean itemPreview = criterion.icon().isItem() && subtitle != null;
        return entityPreview || itemPreview;
    }

    private Component previewSubtitle(CriterionSnapshot criterion) {
        if (criterion == null) {
            return null;
        }

        String key = stripNamespace(criterion.key());
        if (criterion.icon() != null && criterion.icon().isEntityPreview()) {
            EntityPreviewSpec spec = criterion.icon().entityPreviewSpec();
            return switch (spec.variantType()) {
                case CAT -> TrackerDataRepository.current().criteriaVisuals().catSources().get(key);
                case WOLF -> TrackerDataRepository.current().criteriaVisuals().wolfSources().get(key);
                case FROG -> TrackerDataRepository.current().criteriaVisuals().entitySources().get(key);
                case NONE -> {
                    Component criterionSpecific = TrackerDataRepository.current().criteriaVisuals().entitySources().get(key);
                    yield criterionSpecific != null
                            ? criterionSpecific
                            : TrackerDataRepository.current().criteriaVisuals().entitySources().get(spec.entityId().getPath());
                }
            };
        }

        Component itemInfo = TrackerDataRepository.current().criteriaVisuals().itemInfoSources().get(key);
        if (itemInfo != null) {
            return itemInfo;
        }

        String trimPattern = trimPatternKey(criterion.key());
        return trimPattern == null ? null : TrackerDataRepository.current().criteriaVisuals().trimSources().get(trimPattern);
    }

    private void renderPreviewOverlay(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (this.previewOverlay == null) {
            return;
        }

        CriterionSnapshot criterion = this.previewOverlay.current();
        if (criterion == null) {
            this.closePreviewOverlay();
            return;
        }

        Component title = criterion.displayName();
        Component subtitle = this.previewSubtitle(criterion);
        CriterionIcon icon = criterion.icon();
        PreviewLayout layout = this.previewLayout();
        if (layout == null) {
            return;
        }

        guiGraphics.fill(0, 0, this.width, this.height, 0x9A160A0A);
        guiGraphics.fill(layout.panelX(), layout.panelY(), layout.panelRight(), layout.panelBottom(), this.menuBackgroundColor().panelColor() | 0xE0000000);
        guiGraphics.outline(layout.panelX(), layout.panelY(), layout.panelWidth(), layout.panelHeight(), 0xFFF0D3A3);
        guiGraphics.centeredText(this.font, title, this.width / 2, layout.panelY() + 5, TEXT_HEADER);
        if (subtitle != null) {
            guiGraphics.centeredText(this.font, subtitle, this.width / 2, layout.panelY() + 15, TEXT_MUTED);
        }

        if (icon.isEntityPreview()) {
            LivingEntity entity = this.previewEntity(icon.entityPreviewSpec());
            if (entity != null) {
                this.renderEntityPortrait(guiGraphics, entity, layout.visualX(), layout.visualY(), layout.visualWidth(), layout.visualHeight(), true);
            }
        } else if (icon.isItem()) {
            this.renderLargeItemPreview(guiGraphics, icon, layout);
        }

        this.renderPreviewControl(guiGraphics, layout.closeButton(), "X", layout.closeButton().contains(mouseX, mouseY));
        if (this.previewOverlay.hasMultiple()) {
            this.renderPreviewControl(guiGraphics, layout.leftArrow(), "<", layout.leftArrow().contains(mouseX, mouseY));
            this.renderPreviewControl(guiGraphics, layout.rightArrow(), ">", layout.rightArrow().contains(mouseX, mouseY));
        }
    }

    private PreviewLayout previewLayout() {
        if (this.previewOverlay == null || this.previewOverlay.current() == null) {
            return null;
        }

        CriterionSnapshot criterion = this.previewOverlay.current();
        Component title = criterion.displayName();
        Component subtitle = this.previewSubtitle(criterion);
        int textHeight = subtitle != null ? 24 : 14;
        int maxVisualWidth = Math.max(96, Math.round(this.width * PREVIEW_MAX_WIDTH_RATIO));
        int maxVisualHeight = Math.max(96, Math.round(this.height * PREVIEW_MAX_HEIGHT_RATIO));
        int textWidth = Math.max(
                this.font.width(title),
                subtitle != null ? this.font.width(subtitle) : 0
        );
        int visualWidth = maxVisualWidth;
        int visualHeight = maxVisualHeight;
        int panelWidth = Math.max(visualWidth, textWidth) + PREVIEW_PANEL_PADDING * 2;
        int panelHeight = visualHeight + PREVIEW_PANEL_PADDING * 2 + textHeight;
        int panelX = (this.width - panelWidth) / 2;
        int panelY = (this.height - panelHeight) / 2;
        int visualX = panelX + PREVIEW_PANEL_PADDING;
        int visualY = panelY + PREVIEW_PANEL_PADDING + textHeight;
        Rect closeButton = new Rect(
                panelX + panelWidth - PREVIEW_CLOSE_SIZE - 5,
                panelY + 5,
                PREVIEW_CLOSE_SIZE,
                PREVIEW_CLOSE_SIZE
        );
        Rect leftArrow = new Rect(
                visualX + 4,
                visualY + (visualHeight - PREVIEW_NAV_HEIGHT) / 2,
                PREVIEW_NAV_WIDTH,
                PREVIEW_NAV_HEIGHT
        );
        Rect rightArrow = new Rect(
                visualX + visualWidth - PREVIEW_NAV_WIDTH - 4,
                visualY + (visualHeight - PREVIEW_NAV_HEIGHT) / 2,
                PREVIEW_NAV_WIDTH,
                PREVIEW_NAV_HEIGHT
        );
        return new PreviewLayout(panelX, panelY, panelWidth, panelHeight, visualX, visualY, visualWidth, visualHeight, closeButton, leftArrow, rightArrow);
    }

    private void renderPreviewControl(GuiGraphicsExtractor guiGraphics, Rect rect, String label, boolean hovered) {
        guiGraphics.fill(rect.x(), rect.y(), rect.right(), rect.bottom(), hovered ? 0xD96A3F34 : 0xB02D1717);
        if (hovered) {
            guiGraphics.fill(rect.x() + 1, rect.y() + 1, rect.right() - 1, rect.bottom() - 1, 0x28FFE6B8);
        }
        guiGraphics.outline(rect.x(), rect.y(), rect.width(), rect.height(), hovered ? 0xFFFFE6B8 : 0xFFF0D3A3);
        guiGraphics.centeredText(
                this.font,
                label,
                rect.x() + rect.width() / 2,
                rect.y() + rect.height() / 2 - 4,
                hovered ? TEXT_PRIMARY : TEXT_HEADER
        );
    }

    private void navigatePreview(int direction) {
        if (this.previewOverlay == null || !this.previewOverlay.hasMultiple()) {
            return;
        }

        this.previewOverlay = this.previewOverlay.navigate(direction);
    }

    private void closePreviewOverlay() {
        if (this.previewOverlay == null) {
            return;
        }

        this.previewOverlay = null;
    }

    private void renderRequirementScrollbar(
            GuiGraphicsExtractor guiGraphics,
            int x,
            int y,
            int width,
            int height,
            int contentHeight,
            int scrollOffset
    ) {
        guiGraphics.fill(x, y, x + width, y + height, 0x5D3A2323);
        guiGraphics.outline(x, y, width, height, 0x8C8E6565);

        int thumbHeight = Math.max(18, Math.round(height * (height / (float) contentHeight)));
        int thumbTravel = Math.max(0, height - thumbHeight);
        int maxScroll = Math.max(1, contentHeight - height);
        int thumbY = y + Math.round(thumbTravel * (scrollOffset / (float) maxScroll));
        guiGraphics.fill(x + 1, thumbY, x + width - 1, thumbY + thumbHeight, 0xC8C79D62);
        guiGraphics.outline(x, thumbY, width, thumbHeight, 0xFFF0D3A3);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private String ellipsize(String text, int width) {
        if (this.font.width(text) <= width) {
            return text;
        }

        String trimmed = this.font.plainSubstrByWidth(text, Math.max(10, width - this.font.width("...")));
        return trimmed + "...";
    }

    private String singleLineText(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        return text.replaceAll("\\s*\\R+\\s*", " ").replaceAll("\\s+", " ").trim();
    }

    private void refreshItemEntries(boolean force) {
        long now = System.currentTimeMillis();
        boolean itemsTabActive = ITEMS_TAB_ID.equals(this.activeRootId);
        if (!force && !itemsTabActive && !this.cachedItemEntries.isEmpty()) {
            return;
        }

        if (!force && now < this.nextItemScanAt && !this.cachedItemEntries.isEmpty()) {
            return;
        }

        this.cachedItemEntries = this.itemProgressService.scanItemObjectives(this.cachedEntries);
        this.nextItemScanAt = now + ITEM_SCAN_INTERVAL_MILLIS;
    }

    private void activateWaypointAction(WaypointAction action, Identifier waypointId) {
        if (action == WaypointAction.ADD) {
            this.waypointInputOverlay = new WaypointInputOverlay(WaypointInputMode.ADD, null, "");
            this.minecraft.textInputManager().startTextInput(this);
            return;
        }

        if (action == WaypointAction.RENAME && waypointId != null) {
            WaypointSnapshot waypoint = this.findWaypoint(waypointId);
            if (waypoint != null) {
                this.waypointInputOverlay = new WaypointInputOverlay(WaypointInputMode.RENAME, waypointId, waypoint.label());
                this.minecraft.textInputManager().startTextInput(this);
            }
            return;
        }

        if (action == WaypointAction.DELETE && waypointId != null) {
            this.waypointService.delete(this.currentWorldKey(), waypointId);
            if (waypointId.equals(this.selectedAdvancementId)) {
                this.selectedAdvancementId = null;
            }
            this.cachedWaypoints = this.waypointService.waypoints(this.currentWorldKey());
            this.refreshData();
        }
    }

    private WaypointSnapshot findWaypoint(Identifier waypointId) {
        for (WaypointSnapshot waypoint : this.cachedWaypoints) {
            if (waypoint.id().equals(waypointId)) {
                return waypoint;
            }
        }
        return null;
    }

    private void appendWaypointInput(String text) {
        if (this.waypointInputOverlay == null || text == null || text.isEmpty()) {
            return;
        }

        String sanitized = text.replaceAll("\\R+", " ");
        String next = this.waypointInputOverlay.text() + sanitized;
        if (next.length() > 40) {
            next = next.substring(0, 40);
        }
        this.waypointInputOverlay = this.waypointInputOverlay.withText(next);
    }

    private void backspaceWaypointInput() {
        if (this.waypointInputOverlay == null || this.waypointInputOverlay.text().isEmpty()) {
            return;
        }

        String text = this.waypointInputOverlay.text();
        int cut = text.offsetByCodePoints(text.length(), -1);
        this.waypointInputOverlay = this.waypointInputOverlay.withText(text.substring(0, cut));
    }

    private void commitWaypointInput() {
        if (this.waypointInputOverlay == null) {
            return;
        }

        WaypointInputOverlay overlay = this.waypointInputOverlay;
        this.waypointInputOverlay = null;
        this.minecraft.textInputManager().stopTextInput(this);
        if (overlay.mode() == WaypointInputMode.ADD) {
            WaypointSnapshot created = this.waypointService.createAtPlayer(
                    this.currentWorldKey(),
                    this.minecraft == null ? null : this.minecraft.player,
                    overlay.text()
            );
            if (created != null) {
                this.activeRootId = WAYPOINTS_TAB_ID;
                this.selectedAdvancementId = created.id();
            }
        } else if (overlay.mode() == WaypointInputMode.RENAME && overlay.waypointId() != null) {
            this.waypointService.rename(this.currentWorldKey(), overlay.waypointId(), overlay.text());
            this.selectedAdvancementId = overlay.waypointId();
        }
        this.cachedWaypoints = this.waypointService.waypoints(this.currentWorldKey());
        this.refreshData();
    }

    private void renderWaypointInputOverlay(GuiGraphicsExtractor guiGraphics) {
        WaypointInputOverlay overlay = this.waypointInputOverlay;
        if (overlay == null) {
            return;
        }

        int panelWidth = Math.min(360, Math.max(220, this.width - OUTER_PADDING * 4));
        int panelHeight = 86;
        int panelX = (this.width - panelWidth) / 2;
        int panelY = (this.height - panelHeight) / 2;
        int inputX = panelX + 14;
        int inputY = panelY + 42;
        int inputWidth = panelWidth - 28;
        this.minecraft.textInputManager().setTextInputArea(inputX, inputY, inputX + inputWidth, inputY + 20);
        Component title = overlay.mode() == WaypointInputMode.ADD
                ? Component.translatable("screen.aaigttracker.waypoints.add_title")
                : Component.translatable("screen.aaigttracker.waypoints.rename_title");

        guiGraphics.fill(0, 0, this.width, this.height, 0x9A160A0A);
        guiGraphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, this.menuBackgroundColor().panelColor() | 0xE0000000);
        guiGraphics.outline(panelX, panelY, panelWidth, panelHeight, 0xFFF0D3A3);
        guiGraphics.centeredText(this.font, title, this.width / 2, panelY + 12, TEXT_HEADER);
        guiGraphics.fill(inputX, inputY, inputX + inputWidth, inputY + 20, 0xB02D1717);
        guiGraphics.outline(inputX, inputY, inputWidth, 20, 0xD4B48A8A);

        String cursor = (this.currentRenderMillis / 500L) % 2L == 0L ? "_" : "";
        guiGraphics.text(
                this.font,
                this.ellipsize(overlay.text() + cursor, inputWidth - 8),
                inputX + 4,
                inputY + 6,
                TEXT_PRIMARY,
                false
        );
        guiGraphics.centeredText(
                this.font,
                Component.translatable("screen.aaigttracker.waypoints.input_hint"),
                this.width / 2,
                panelY + panelHeight - 14,
                TEXT_MUTED
        );
    }

    private void renderLargeItemPreview(GuiGraphicsExtractor guiGraphics, CriterionIcon icon, PreviewLayout layout) {
        int targetItemSize = Math.min(Math.min(layout.visualWidth(), layout.visualHeight()), PREVIEW_ITEM_TARGET_SIZE);
        float itemScale = targetItemSize / (float) PREVIEW_ITEM_BASE_SIZE;
        int itemX = layout.panelX() + (layout.panelWidth() - targetItemSize) / 2;
        int itemY = layout.visualY() + Math.max(0, (layout.visualHeight() - targetItemSize) / 2);
        var pose = guiGraphics.pose();
        pose.pushMatrix();
        pose.translate(itemX, itemY);
        pose.scale(itemScale, itemScale);
        guiGraphics.item(icon.copyItemStack(), 0, 0);
        pose.popMatrix();
    }

    private boolean renderBiomeSprite(
            GuiGraphicsExtractor guiGraphics,
            BiomeSpriteSpec previewSpec,
            int x,
            int y,
            int slotWidth,
            int slotHeight
    ) {
        if (previewSpec == null || previewSpec.isEmpty()) {
            return false;
        }

        Identifier spriteId = biomeSpriteId(previewSpec.biomeId());
        boolean firstDraw = !this.drawnBiomeSprites.contains(spriteId);
        // First use decodes and uploads a texture. Spread that work over frames when opening a biome list.
        if (firstDraw && this.biomeSpriteLoadsThisFrame >= BIOME_SPRITE_LOADS_PER_FRAME) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getResourceManager() == null || minecraft.getResourceManager().getResource(spriteId).isEmpty()) {
            return false;
        }

        if (firstDraw) {
            this.biomeSpriteLoadsThisFrame++;
            this.drawnBiomeSprites.add(spriteId);
        }

        int spriteSize = Math.max(1, Math.min(BIOME_SPRITE_SIZE, Math.min(slotWidth, slotHeight)));
        int spriteX = x + Math.max(0, (slotWidth - spriteSize) / 2);
        int spriteY = y + Math.max(0, (slotHeight - spriteSize) / 2);
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                spriteId,
                spriteX,
                spriteY,
                0.0F,
                0.0F,
                spriteSize,
                spriteSize,
                BIOME_SPRITE_SIZE,
                BIOME_SPRITE_SIZE,
                BIOME_SPRITE_SIZE,
                BIOME_SPRITE_SIZE
        );
        return true;
    }

    private static Identifier biomeSpriteId(Identifier biomeId) {
        String fileName = biomeId.getNamespace() + "_" + biomeId.getPath().replace('/', '_') + ".png";
        return Identifier.fromNamespaceAndPath("aaigttracker", "biome_sprites/" + fileName);
    }

    private void renderEntityPortrait(
            GuiGraphicsExtractor guiGraphics,
            LivingEntity entity,
            int x,
            int y,
            int width,
            int height,
            boolean rotating
    ) {
        float yaw = rotating
                ? (this.currentRenderMillis % PREVIEW_ROTATION_PERIOD_MILLIS) * 360.0F / PREVIEW_ROTATION_PERIOD_MILLIS
                : 35.0F;
        boolean animated = rotating || entity.getType() == EntityTypes.FROG;
        float ageInTicks = animated ? (this.currentRenderMillis % 120_000L) / 50.0F : 0.0F;
        entity.setPos(0, 0, 0);
        entity.tickCount = (int) ageInTicks;
        this.stabilizePreviewEntity(entity);
        this.applyPreviewRotation(entity);

        EntityRenderState renderState = this.extractPreviewRenderState(entity, yaw, ageInTicks, animated);
        if (renderState == null) {
            return;
        }

        int renderX = x;
        int renderY = y;
        int renderWidth = width;
        int renderHeight = height;
        if (!rotating) {
            renderX = x + 2;
            renderY = y + 2;
            renderWidth = Math.max(1, width - 4);
            renderHeight = Math.max(1, height - 4);
            guiGraphics.enableScissor(x, y, x + width, y + height);
        }

        float size = rotating
                ? this.modalEntityRenderSize(renderState, renderWidth, renderHeight)
                : this.rowEntityRenderSize(renderState, renderWidth, renderHeight);
        Vector3f offset = new Vector3f(0.0F, this.previewVerticalOffset(renderState, rotating), 0.0F);
        Quaternionf baseRotation = new Quaternionf()
                .rotateZ((float) Math.PI)
                .rotateX((float) Math.toRadians(PREVIEW_CAMERA_X_ROTATION_DEGREES));
        if (renderState.entityType == EntityTypes.ENDER_DRAGON && rotating) {
            baseRotation.rotateY((float) Math.toRadians(yaw));
        }
        Quaternionf cameraTilt = new Quaternionf().rotateX((float) Math.toRadians(PREVIEW_CAMERA_X_ROTATION_DEGREES));

        guiGraphics.entity(
                renderState,
                size,
                offset,
                baseRotation,
                cameraTilt,
                renderX,
                renderY,
                renderX + renderWidth,
                renderY + renderHeight
        );
        if (!rotating) {
            guiGraphics.disableScissor();
        }
    }

    private void stabilizePreviewEntity(LivingEntity entity) {
        if (entity instanceof Hoglin hoglin) {
            hoglin.setImmuneToZombification(true);
            hoglin.setTimeInOverworld(0);
        }
        if (entity instanceof AbstractPiglin piglin) {
            piglin.setImmuneToZombification(true);
            piglin.setTimeInOverworld(0);
        }
    }

    private void applyPreviewRotation(LivingEntity entity) {
        entity.setYRot(0.0F);
        entity.yRotO = 0.0F;
        entity.setYBodyRot(0.0F);
        entity.yBodyRot = 0.0F;
        entity.yBodyRotO = 0.0F;
        entity.setYHeadRot(0.0F);
        entity.yHeadRot = 0.0F;
        entity.yHeadRotO = 0.0F;
        entity.setXRot(0.0F);
        entity.xRotO = 0.0F;
    }

    private EntityRenderState extractPreviewRenderState(LivingEntity entity, float yaw, float ageInTicks, boolean animated) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return null;
        }

        EntityRenderState renderState = minecraft.getEntityRenderDispatcher().extractEntity(entity, 1.0F);
        renderState.ageInTicks = ageInTicks;
        renderState.shadowPieces.clear();

        if (renderState instanceof LivingEntityRenderState livingState) {
            float scale = livingState.scale == 0.0F ? 1.0F : livingState.scale;
            livingState.boundingBoxWidth /= scale;
            livingState.boundingBoxHeight /= scale;
            livingState.scale = 1.0F;
            livingState.bodyRot = 180.0F + yaw;
            livingState.yRot = 0.0F;
            livingState.xRot = 0.0F;
            livingState.walkAnimationPos = animated ? ageInTicks * 0.08F : 0.0F;
            livingState.walkAnimationSpeed = animated ? 0.08F : 0.0F;
        }

        if (renderState instanceof EnderDragonRenderState dragonState) {
            dragonState.flapTime = animated ? ageInTicks * 0.045F : 0.0F;
            dragonState.partialTicks = animated ? 1.0F : 0.0F;
        }

        if (renderState instanceof FrogRenderState frogState && animated) {
            int cycleStart = (int) (ageInTicks / 60.0F) * 60;
            if (ageInTicks - cycleStart < 20.0F) {
                frogState.jumpAnimationState.start(cycleStart);
            } else if (ageInTicks - cycleStart < 40.0F) {
                frogState.croakAnimationState.start(cycleStart + 20);
            }
        }

        renderState.boundingBoxWidth = Math.max(0.1F, renderState.boundingBoxWidth);
        renderState.boundingBoxHeight = Math.max(0.1F, renderState.boundingBoxHeight);
        return renderState;
    }

    private float rowEntityRenderSize(EntityRenderState renderState, int width, int height) {
        float widthFit = width * 0.86F / this.entityFitWidth(renderState);
        float heightFit = height * 0.90F / renderState.boundingBoxHeight;
        return Mth.clamp(Math.min(widthFit, heightFit), 2.0F, 22.0F);
    }

    private float modalEntityRenderSize(EntityRenderState renderState, int width, int height) {
        float widthRatio = renderState.entityType == EntityTypes.ENDER_DRAGON ? 0.82F : 0.56F;
        float heightRatio = renderState.entityType == EntityTypes.ENDER_DRAGON ? 0.72F : 0.76F;
        float maxSize = renderState.entityType == EntityTypes.ENDER_DRAGON ? 120.0F : 210.0F;
        float widthFit = width * widthRatio / this.entityFitWidth(renderState);
        float heightFit = height * heightRatio / renderState.boundingBoxHeight;
        return Mth.clamp(Math.min(widthFit, heightFit), 18.0F, maxSize);
    }

    private float previewVerticalOffset(EntityRenderState renderState, boolean rotating) {
        if (renderState.entityType == EntityTypes.ENDER_DRAGON) {
            return renderState.boundingBoxHeight * (rotating ? 0.50F : 0.60F);
        }
        if (!rotating) {
            return renderState.boundingBoxHeight * 0.62F;
        }
        return renderState.boundingBoxHeight / 2.0F;
    }

    private float entityFitWidth(EntityRenderState renderState) {
        float width = Math.max(0.1F, renderState.boundingBoxWidth);
        if (renderState.entityType == EntityTypes.ENDER_DRAGON) {
            return width * 1.85F;
        }
        if (renderState.entityType == EntityTypes.GHAST || renderState.entityType == EntityTypes.PHANTOM) {
            return width * 1.35F;
        }
        return width;
    }

    private LivingEntity previewEntity(EntityPreviewSpec previewSpec) {
        if (previewSpec == null) {
            return null;
        }

        LivingEntity cached = this.previewEntities.get(previewSpec);
        if (cached != null) {
            return cached;
        }

        LivingEntity created = this.createPreviewEntity(previewSpec);
        if (created != null) {
            this.previewEntities.put(previewSpec, created);
        }
        return created;
    }

    private LivingEntity createPreviewEntity(EntityPreviewSpec previewSpec) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.level == null) {
            return null;
        }

        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(previewSpec.entityId()).orElse(null);
        if (entityType == null) {
            return null;
        }

        Entity entity = entityType.create(minecraft.level, EntitySpawnReason.COMMAND);
        if (!(entity instanceof LivingEntity livingEntity)) {
            return null;
        }

        // GUI entities never join the world, so 26.2 does not assign their IDs automatically.
        livingEntity.setId(NEXT_PREVIEW_ENTITY_ID.getAndDecrement());
        this.applyPreviewVariant(livingEntity, previewSpec);
        this.stabilizePreviewEntity(livingEntity);
        livingEntity.setYRot(0.0F);
        livingEntity.setXRot(0.0F);
        livingEntity.setYHeadRot(0.0F);
        livingEntity.setYBodyRot(0.0F);
        return livingEntity;
    }

    private void applyPreviewVariant(LivingEntity entity, EntityPreviewSpec previewSpec) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.level == null || previewSpec.variantId() == null) {
            return;
        }

        switch (previewSpec.variantType()) {
            case CAT -> {
                var registry = minecraft.level.registryAccess().lookupOrThrow(Registries.CAT_VARIANT);
                var variant = registry.get(previewSpec.variantId()).orElse(null);
                if (variant != null) {
                    entity.setComponent(DataComponents.CAT_VARIANT, variant);
                }
            }
            case WOLF -> {
                var registry = minecraft.level.registryAccess().lookupOrThrow(Registries.WOLF_VARIANT);
                var variant = registry.get(previewSpec.variantId()).orElse(null);
                if (variant != null) {
                    entity.setComponent(DataComponents.WOLF_VARIANT, variant);
                }
            }
            case FROG -> {
                var registry = minecraft.level.registryAccess().lookupOrThrow(Registries.FROG_VARIANT);
                var variant = registry.get(previewSpec.variantId()).orElse(null);
                if (variant != null) {
                    entity.setComponent(DataComponents.FROG_VARIANT, variant);
                }
            }
            case NONE -> {
            }
        }
    }

    private boolean isAdvancementCriterionComplete(Identifier advancementId, String criterionKey) {
        if (advancementId == null || criterionKey == null) {
            return false;
        }

        for (AdvancementSnapshot snapshot : this.cachedEntries) {
            if (!snapshot.id().equals(advancementId)) {
                continue;
            }
            for (CriterionSnapshot criterion : snapshot.criteria()) {
                String key = criterion.key();
                if ((criterionKey.equals(key) || criterionKey.equals(stripNamespace(key))) && criterion.completed()) {
                    return true;
                }
            }
            return snapshot.done();
        }
        return false;
    }

    private static String trimCriterionKey(String pattern) {
        return "armor_trimmed_minecraft:" + pattern + "_armor_trim_smithing_template_smithing_trim";
    }

    private static String trimPatternKey(String criterionKey) {
        if (criterionKey == null || !criterionKey.startsWith("armor_trimmed_")) {
            return null;
        }

        int namespaceIndex = criterionKey.indexOf(':');
        String key = namespaceIndex >= 0 && namespaceIndex + 1 < criterionKey.length()
                ? criterionKey.substring(namespaceIndex + 1)
                : criterionKey.substring("armor_trimmed_".length());
        String suffix = "_armor_trim_smithing_template_smithing_trim";
        return key.endsWith(suffix) ? key.substring(0, key.length() - suffix.length()) : null;
    }

    private static String stripNamespace(String value) {
        if (value == null) {
            return "";
        }

        int separatorIndex = value.indexOf(':');
        return separatorIndex >= 0 && separatorIndex + 1 < value.length()
                ? value.substring(separatorIndex + 1)
                : value;
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static String formatMillis(Long millis) {
        if (millis == null) {
            return "--:--:--.--";
        }

        long hours = millis / 3_600_000L;
        long minutes = (millis / 60_000L) % 60L;
        long seconds = (millis / 1_000L) % 60L;
        long centiseconds = (millis / 10L) % 100L;
        return String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, centiseconds);
    }

    private enum WaypointAction {
        ADD,
        RENAME,
        DELETE
    }

    private enum SettingsAction {
        SET_VISIBILITY,
        SET_BACKGROUND,
        TOGGLE_NO_FOG,
        TOGGLE_NO_DARKNESS,
        TOGGLE_FULLBRIGHT
    }

    private record SettingsActionHitbox(
            SettingsAction action,
            Rect rect,
            CompletionVisibilityMode visibilityMode,
            MenuBackgroundColor backgroundColor
    ) {
        private static SettingsActionHitbox visibility(Rect rect, CompletionVisibilityMode visibilityMode) {
            return new SettingsActionHitbox(SettingsAction.SET_VISIBILITY, rect, visibilityMode, null);
        }

        private static SettingsActionHitbox background(Rect rect, MenuBackgroundColor backgroundColor) {
            return new SettingsActionHitbox(SettingsAction.SET_BACKGROUND, rect, null, backgroundColor);
        }

        private static SettingsActionHitbox simple(Rect rect, SettingsAction action) {
            return new SettingsActionHitbox(action, rect, null, null);
        }
    }

    private enum WaypointInputMode {
        ADD,
        RENAME
    }

    private record WaypointActionHitbox(WaypointAction action, Identifier waypointId, Rect rect) {
    }

    private record WaypointInputOverlay(WaypointInputMode mode, Identifier waypointId, String text) {
        private WaypointInputOverlay {
            text = text == null ? "" : text;
        }

        private WaypointInputOverlay withText(String text) {
            return new WaypointInputOverlay(this.mode, this.waypointId, text);
        }
    }
}
