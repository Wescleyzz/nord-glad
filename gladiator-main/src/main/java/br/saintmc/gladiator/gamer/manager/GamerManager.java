package br.saintmc.gladiator.gamer.manager;

import br.saintmc.gladiator.gamer.Gamer;
import lombok.Getter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
public class GamerManager {

    private Map<UUID, Gamer> gamerMap = new HashMap();

    public void loadGamer(UUID uuid, Gamer gamer) {
        if (this.gamerMap.containsKey(uuid)) {
            return;
        }
        this.gamerMap.put(uuid, gamer);
    }

    public void unloadGamer(UUID uuid) {
        if (!this.gamerMap.containsKey(uuid)) {
            return;
        }
        this.gamerMap.remove(uuid);
    }

    public Gamer getGamer(UUID uuid) { return (Gamer) this.gamerMap.get(uuid); }

    public Collection<Gamer> filter(Predicate<? super Gamer> predicate) { return (Collection)this.gamerMap.values().stream().filter(predicate).collect(Collectors.toList()); }

    public Collection<Gamer> getGamers() { return this.gamerMap.values(); }
}
