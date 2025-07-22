export async function getNewGameId() {
    try {
        const response = await fetch('/new-game-id');
        const data = await response.json();
        console.log(data)
        return data.id;
    } catch (error) {
        console.error("can't fetch new-game-id:", error);
    }
}

export async function getInitialGameState(gameId) {
    try {
        const response = await fetch(`/game-state/${gameId}`);
        const data = await response.json();
        console.log(data)
        return data;
    } catch (error) {
        console.error("can't fetch initial game state:", error);
    }
}

export async function activateGame(gameId) {
    try {
        await fetch(`/game-state/${gameId}`, {method: 'POST'});
    } catch (error) {
        console.error("can't fetch initial game state:", error);
    }
}

export async function getUpgradesAndSpells() {
    try {
        const response = await fetch('/upgrades-and-spells');
        const data = await response.json();
        console.log(data)
        return data;
    } catch (error) {
        console.error("can't fetch new-game-id:", error);
    }
}

export async function playUpgrade(gameId) {
    try {
        await fetch(`/game-state/${gameId}/upgrade`, {
            method: 'POST',
            body: JSON.stringify({
                key1: 'value1',
                key2: 'value2'
            })
        });
    } catch (error) {
        console.error("can't play upgrade:", error);
    }
}

export async function playSpell(gameId) {
    try {
        await fetch(`/game-state/${gameId}/spell`, {
            method: 'POST',
            body: JSON.stringify({
                key1: 'value1',
                key2: 'value2'
            })
        });
    } catch (error) {
        console.error("can't play spell:", error);
    }
}
