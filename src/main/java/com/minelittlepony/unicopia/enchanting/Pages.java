package com.minelittlepony.unicopia.enchanting;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.Unicopia;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.SystemUtil;
import net.minecraft.util.profiler.Profiler;

public class Pages extends JsonDataLoader implements IdentifiableResourceReloadListener {
    private static final Identifier ID = new Identifier(Unicopia.MODID, "pages");
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static final Pages instance = new Pages();

    public static Pages instance() {
        return instance;
    }

    private final Map<Identifier, PageInstance> pages = Maps.newHashMap();
    private List<PageInstance> pagesByIndex = Lists.newArrayList();

    private final Map<String, IConditionFactory> conditionFactories = SystemUtil.consume(Maps.newHashMap(), m -> {
        m.put("unicopia:compound_condition", CompoundCondition::new);
        m.put("unicopia:page_state", PageStateCondition::new);
        m.put("unicopia:spell_crafting", SpellCraftingEvent.Condition::new);
    });

    Pages() {
        super(GSON, "pages");
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    protected Map<Identifier, JsonObject> prepare(ResourceManager manager, Profiler profiler) {
        // TODO: broken synthetic
        return super.method_20731(manager, profiler);
    }

    @Override
    protected void apply(Map<Identifier, JsonObject> data, ResourceManager manager, Profiler profiled) {
        pages.clear();
        data.forEach((id, json) -> pages.put(id, new PageInstance(id, json)));

        pagesByIndex = pages.values().stream().sorted(this::comparePages).collect(Collectors.toList());

        int i = 0;

        for (PageInstance page : pagesByIndex) {
            page.index = i++;
        }
    }

    protected int comparePages(PageInstance a, PageInstance b) {
        if (a.parent == null && b.parent == null) {
            return 0;
        }

        if (a.parent == null) {
            return -1;
        }

        if (b.parent == null) {
            return 1;
        }

        if (a.parent.equals(b.name)) {
            return 1;
        }

        if (b.parent.equals(a.name)) {
            return -1;
        }

        return a.name.compareTo(b.name);
    }

    @SuppressWarnings("unchecked")
    <T extends IUnlockEvent> IUnlockCondition<T> createCondition(JsonObject json) {
        String key = json.get("key").getAsString();

        return (IUnlockCondition<T>)conditionFactories.get(key).create(json);
    }

    @Nullable
    public Page getByName(Identifier name) {
        return pages.get(name);
    }

    @Nullable
    public Page getByIndex(int index) {
        return pagesByIndex.get(index);
    }

    public Stream<Page> getUnlockablePages(Predicate<Page> predicate) {
        return pages.values().stream().map(Page.class::cast).filter(predicate);
    }

    public void triggerUnlockEvent(PageOwner owner, IUnlockEvent event, @Nullable IPageUnlockListener unlockListener) {
        pages.values().stream()
            .filter(page -> page.canUnlock(owner, event))
            .forEach(page -> unlockPage(owner, page, unlockListener));
    }

    public void unlockPage(PageOwner owner, Page page, @Nullable IPageUnlockListener unlockListener) {
        if (owner.getPageState(page).isLocked()) {
            if (unlockListener == null || unlockListener.onPageUnlocked(page)) {
                owner.setPageState(page, PageState.UNREAD);
            }
        }
    }

    public int getTotalPages() {
        return pages.size();
    }
}
