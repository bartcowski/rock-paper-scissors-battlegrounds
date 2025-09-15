import { activateGame, getInitialGameState, getGameState, getNewGameId, getUpgradesAndSpells, playSpell, playUpgrade } from "./api.js";

//references to screen elements
const mainMenu = document.getElementById("mainMenu");
const gameScreen = document.getElementById("gameScreen");
const hudChooseUpgradesAndSpells = document.getElementById("hudChooseUpgradesAndSpells");
const hudBeginSimulation = document.getElementById("hudBeginSimulation");
const gameOverScreen = document.getElementById("gameOverScreen");
const canvas = document.getElementById("gameCanvas");
const ctx = canvas.getContext("2d");

document.getElementById("startButton").addEventListener("click", startGame);
document.getElementById("beginSimulationButton").addEventListener("click", startBattle);
document.getElementById("restartGameButton").addEventListener("click", restartGame);
let canvasClickHandler; //need it in outer scope, to add and remove from different functions

let mouseX = 0;
let mouseY = 0;
canvas.addEventListener("mousemove", (event) => {
  const boundingRect = canvas.getBoundingClientRect();
  mouseX = event.clientX - boundingRect.left;
  mouseY = event.clientY - boundingRect.top;
});

let socket = null;

//game constants
const SYMBOL_SIZE = 30;
const ROCK = 'ROCK'
const PAPER = 'PAPER'
const SCISSORS = 'SCISSORS'

//preload assets
const rockImg = new Image();
let rockImgLoaded = false;
rockImg.onload = () => {
  rockImgLoaded = true;
};
rockImg.src = "assets/rock.png";

const paperImg = new Image();
let paperImgLoaded = false;
paperImg.onload = () => {
  paperImgLoaded = true;
};
paperImg.src = "assets/paper.png";

const scissorsImg = new Image();
let scissorsImgLoaded = false;
scissorsImg.onload = () => {
  scissorsImgLoaded = true;
};
scissorsImg.src = "assets/scissors.png";

//game state
let isRunning = false;
let gameLoopId;
let gameId = null;
let symbols = [];
let players = [
  {
    name: ROCK,
    actions: []
  },
  {
    name: PAPER,
    actions: []
  },
  {
    name: SCISSORS,
    actions: []
  }
];

const ActionType = {
  UPGRADE: 'upgrade',
  SPELL: 'spell'
};

//select phase state
const SelectPhase = {
  ACTION: 'action',
  SYMBOL: 'symbol'
};
let currentActionCounter = 0;
let selectingPlayer;
let selectedAction;

// ============================= SET UP GAME FUNCTIONS =============================

function startGame() {
  mainMenu.style.display = "none";
  gameOverScreen.style.display = "none";
  gameScreen.style.display = "block";

  isRunning = true;
  initGame();
  gameLoop();
}

async function initGame() {
  gameId = await getNewGameId();
  console.log(`game init, ID of the game: ${gameId}`);

  const initialGameState = await getInitialGameState(gameId);
  symbols = initialGameState.symbols;
  console.log(`initial positions set for ${symbols.length} symbols`);

  const upgradesAndSpells = await getUpgradesAndSpells();
  initPlayers(upgradesAndSpells);

  console.log(players);
  
  hudChooseUpgradesAndSpells.style.display = "block";

  startSelectPhase();
}

function gameLoop(timestamp) {
  if (!isRunning) return;
  if (!rockImgLoaded || !scissorsImgLoaded || !scissorsImgLoaded) {
    requestAnimationFrame(gameLoop);
    return;
  }
  ctx.clearRect(0, 0, canvas.width, canvas.height);

  ctx.fillStyle = "#ccffcc";
  ctx.fillRect(0, 0, canvas.width, canvas.height);

  for (const symbol of symbols) {
    const image = determineImg(symbol.type);
    if (!image) {
      continue;
    }

    const isSymbolHovered =
    selectedAction &&
    selectedAction.type === ActionType.UPGRADE &&
    mouseX >= symbol.position.x &&
    mouseX <= symbol.position.x + SYMBOL_SIZE &&
    mouseY >= symbol.position.y &&
    mouseY <= symbol.position.y + SYMBOL_SIZE &&
    symbol.type === selectingPlayer.name;
    if (isSymbolHovered) {
      ctx.beginPath();
      ctx.arc(
        symbol.position.x + SYMBOL_SIZE / 2,
        symbol.position.y + SYMBOL_SIZE / 2,
        SYMBOL_SIZE / 1.5,
        0,
        Math.PI * 2
      );
      ctx.fillStyle = "rgba(255, 229, 229, 0.8)";
      ctx.fill();

      ctx.strokeStyle = "red";
      ctx.lineWidth = 2;
      ctx.stroke();
    }

    // TODO: maybe it could show which symbols are affected?
    const isSpellHovered = selectedAction && selectedAction.type === ActionType.SPELL
    if (isSpellHovered) {
      ctx.beginPath();
      ctx.arc(
        mouseX,
        mouseY,
        75, //TODO: establish spell radius
        0,
        Math.PI * 2
      );
      ctx.fillStyle = "rgba(255, 229, 229, 0.02)";
      ctx.fill();

      ctx.strokeStyle = "red";
      ctx.lineWidth = 2;
      ctx.stroke();
    }

    ctx.drawImage(image, symbol.position.x, symbol.position.y, SYMBOL_SIZE, SYMBOL_SIZE);
  }

  gameLoopId = requestAnimationFrame(gameLoop);
}

function determineImg(type) {
  if (type === ROCK) {
    return rockImg;
  } else if (type === PAPER) {
    return paperImg;
  } else if (type === SCISSORS) {
    return scissorsImg;
  } else {
    console.error(`unkown symbol type: ${type}`);
    return null;
  }
}

function initPlayers(upgradesAndSpells) {
  const upgradeNames = upgradesAndSpells.upgrades;
  const spellNames = upgradesAndSpells.spells;

  for (const player of players) {
    for (const upgradeName of upgradeNames) {
      player.actions.push({name: upgradeName, type: ActionType.UPGRADE, used: false});
    }
    for (const spellName of spellNames) {
      player.actions.push({name: spellName, type: ActionType.SPELL, used: false});
    }
  }
}

function restartGame() {
  currentActionCounter = 0;
  for (const player of players) {
    player.actions = [];
  }
  symbols = [];
  selectedAction = null;
  gameOverScreen.style.display = "none";
  mainMenu.style.display = "block";
}

function endGame(winner) {
  isRunning = false;
  cancelAnimationFrame(gameLoopId);
  const winnerText = document.getElementById('winnerInfo')
  winnerText.textContent = `winner: ${winner}`;

  gameScreen.style.display = "none";
  hudBeginSimulation.style.display = "none";
  gameOverScreen.style.display = "block";
}

// ============================= SELECT PHASE FUNCTIONS =============================

//TODO: 
// display already played upgrades and spells somehow
// only symbols are taken from fetched gamestates, spells need to be included client-side (in a separate object probably "const spells = []" and then add it to game loop)
// for multiplayer mode there needs to be UI saying that other player is choosing (so that other players' buttons are not displayed for everybody)

function startSelectPhase() {
  selectingPlayer = players[currentActionCounter]
  canvasClickHandler = (event) => handleCanvasClick(event)
  canvas.addEventListener('click', canvasClickHandler)
  renderUpgradeAndSpellButtons()
}

function renderUpgradeAndSpellButtons() {
    const hudChooseUpgradesAndSpells = document.getElementById("hudChooseUpgradesAndSpells");
    hudChooseUpgradesAndSpells.innerHTML = "";
    const currentPlayerText = document.createElement("p");
    currentPlayerText.textContent = `current player: ${selectingPlayer.name}`;
    hudChooseUpgradesAndSpells.appendChild(currentPlayerText);
    for (const action of selectingPlayer.actions) {
        const btn = document.createElement("button");
        btn.textContent = action.name;
        if (action.used) {
          btn.disabled = true;
        }
        btn.classList.add("action-btn");
        btn.addEventListener("click", () => handlePlayerActionClick(action, btn));
        hudChooseUpgradesAndSpells.appendChild(btn);
    }
}

function handlePlayerActionClick(action, button) {
  console.log(`player ${selectingPlayer.name} clicked action: ${action.name}`);
  selectedAction = action;
  document.querySelectorAll(".action-btn").forEach(b => b.classList.remove("clickedActionButton"));
  button.classList.add("clickedActionButton");
}

async function handleCanvasClick(event) {
  if (!selectedAction) {
    return;
  }

  const boundingRect = canvas.getBoundingClientRect();
  const x = event.clientX - boundingRect.left;
  const y = event.clientY - boundingRect.top;
  console.log({x, y});

  if (selectedAction.type === ActionType.UPGRADE) {
    for (const symbol of symbols) {
      const symbolX = symbol.position.x;
      const symbolY = symbol.position.y;

      // TODO: add validation to backend and respond with info if success or fail
      if (isSymbolClicked(x, y, symbolX, symbolY) && symbol.type === selectingPlayer.name && !symbol.upgrade) {
        console.log("clicked " + symbol.type);
        await playUpgrade(gameId, symbol.id, selectedAction.name, selectingPlayer.name); // TODO: this could return updated game state

        const newGameState = await getGameState(gameId);
        symbols = newGameState.symbols

        moveToNextPlayerOrFinishSelectPhase();
      }
    }
    //console.warn('UPGRADE MUST BE PLAYED ON A SYMBOL!');
  } else {
    await playSpell(gameId, selectedAction.name, x, y, selectingPlayer.name);
    moveToNextPlayerOrFinishSelectPhase();
  }
}

function isSymbolClicked(clickX, clickY, symbolX, symbolY) {
  return clickX > symbolX && 
    clickX < symbolX + SYMBOL_SIZE && 
    clickY > symbolY && 
    clickY < symbolY + SYMBOL_SIZE;
}

function moveToNextPlayerOrFinishSelectPhase() {
  selectingPlayer.actions.find(playerAction => playerAction.name === selectedAction.name).used = true;
  currentActionCounter += 1;
  selectedAction = null;

  if (currentActionCounter % 3 === 0 && !selectingPlayer.actions.some(action => action.used === false)) {
    canvas.removeEventListener('click', canvasClickHandler);
    renderStartBattleButton();
  } else {
    selectingPlayer = players[currentActionCounter % 3]
    renderUpgradeAndSpellButtons();
  }
}

function renderStartBattleButton() {
  hudChooseUpgradesAndSpells.style.display = "none";
  hudBeginSimulation.style.display = "block";
}

// ============================= START BATTLE FUNCTIONS =============================

async function startBattle() {
  const protocol = window.location.protocol === "https:" ? "wss" : "ws";
  const host = window.location.host;
  const url = `${protocol}://${host}/ws/game-state/${gameId}`
  socket = new WebSocket(url);
  socket.onopen = () => {
    console.log(`socket connection established for ${url}`);
  }
  socket.onmessage = (event) => {
    //read about interpolation for smoother movement!
    const json = JSON.parse(event.data);
    console.log(`received ws data of ${json.symbols.length} symbols`);

    if (json.status === 'ENDED') {
      endGame(json.winner)
    }
    symbols = json.symbols;
  }

  await activateGame(gameId);
}
