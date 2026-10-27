package net.montoyo.wd.utilities.browser;

import com.cinemamod.mcef.MCEFBrowser;
import com.cinemamod.mcef.MCEFClient;
import org.cef.event.CefMouseEvent;
import net.montoyo.wd.entity.ScreenBlockEntity;
import net.montoyo.wd.utilities.browser.handlers.js.queries.ElementCenterQuery;
import net.montoyo.wd.utilities.browser.handlers.js.JSQueryHandler;
import net.montoyo.wd.utilities.data.BlockSide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Set;

public class WDClientBrowser extends MCEFBrowser implements WDBrowser {
    ElementCenterQuery focusedEl = new ElementCenterQuery("ActiveElement", "document.activeElement");
    ElementCenterQuery pointerLockEl =
            new ElementCenterQuery("PointerElement", "document.pointerLockElement")
                    .addAdditional("unadjust", "document.webdisplays__unadjustPointerMotion")
            ;
    HashMap<String, JSQueryHandler> handlerHashMap = new HashMap<>();

    ScreenBlockEntity be;
    BlockSide side;

    // Every browser not yet really closed (main thread only). Lets the client close leaked
    // browsers on disconnect and sweep orphans, see ClientProxy.
    private static final Set<WDClientBrowser> LIVE = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final long PENDING_CLOSE_TIMEOUT_MS = 30_000;
    private final long createdMs = System.currentTimeMillis();
    private boolean closeRequested;
    private boolean closed;

    public WDClientBrowser(MCEFClient client, String url, boolean transparent) {
        super(client, url, transparent);
        LIVE.add(this);
    }

    /**
     * JCEF's close(boolean) marks the browser as closing, then asks the native side to close it.
     * While Chromium is still creating the browser (asynchronously, after createImmediately())
     * there is no native browser yet, so the native close is a silent no-op — and since the
     * closing flag is already set, every later close() is ignored too. The browser then finishes
     * creating and keeps playing audio with nothing referencing it, even after disconnecting.
     * So: if the native browser doesn't exist yet, defer the close until it does (see
     * {@link #tickPendingCloses()}). Also routes through MCEF's close(), which frees the GL texture.
     */
    @Override
    public void close(boolean force) {
        if (closed) return;
        closeRequested = true;
        if (getNativeRef("CefBrowser") == 0 && System.currentTimeMillis() - createdMs < PENDING_CLOSE_TIMEOUT_MS)
            return; // retried by tickPendingCloses()

        closed = true;
        LIVE.remove(this);
        super.close();
    }

    public boolean isCloseRequested() {
        return closeRequested;
    }

    /** Retries closes deferred because the native browser didn't exist yet. Call every client tick. */
    public static void tickPendingCloses() {
        if (LIVE.isEmpty()) return;
        for (WDClientBrowser b : new ArrayList<>(LIVE)) {
            if (b.closeRequested)
                b.close(true);
        }
    }

    /** Snapshot of the browsers that are still alive (or waiting for a deferred close). */
    public static ArrayList<WDClientBrowser> liveBrowsers() {
        return new ArrayList<>(LIVE);
    }

    @Override
    public HashMap<String, JSQueryHandler> queryHandlers() {
        return handlerHashMap;
    }

    @Override
    public ElementCenterQuery focusedElement() {
        return focusedEl;
    }

    @Override
    public ElementCenterQuery pointerLockElement() {
        return pointerLockEl;
    }

    @Override
    public void setBe(ScreenBlockEntity blockEntity, BlockSide side) {
        this.be = blockEntity;
        this.side = side;
    }

    @Override
    public ScreenBlockEntity getBe() {
        return be;
    }

    @Override
    public BlockSide getSide() {
        return side;
    }

    /**
     * Public wrapper for the protected sendMouseEvent method.
     * Needed for drag operations that require mouse modifiers.
     */
    public void sendMouseEventPublic(CefMouseEvent event) {
        sendMouseEvent(event);
    }
}
