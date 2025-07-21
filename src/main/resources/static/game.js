import { activateGame, getInitialGameState, getNewGameId, getUpgradesAndSpells } from "./api.js";

//references to screen elements
const mainMenu = document.getElementById("mainMenu");
const gameScreen = document.getElementById("gameScreen");
const hudChooseUpgradesAndSpells = document.getElementById("hudChooseUpgradesAndSpells");
const hudBeginSimulation = document.getElementById("hudBeginSimulation");
const gameOverScreen = document.getElementById("gameOverScreen");
const canvas = document.getElementById("gameCanvas");
const ctx = canvas.getContext("2d");

document.getElementById("startButton").addEventListener("click", startGame);
document.getElementById("beginSimulationButton").addEventListener("click", beginSimulation);

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
    name: "Rock",
    actions: []
  },
  {
    name: "Paper",
    actions: []
  },
  {
    name: "Scissors",
    actions: []
  }
]
let currentActionCounter = 0

let socket = null;

//functions
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
  symbols = initialGameState.symbols
  console.log(`initial positions set for ${symbols.length} symbols`)

  const upgradesAndSpells = await getUpgradesAndSpells()
  initPlayers(upgradesAndSpells)

  console.log(players)
  
  hudChooseUpgradesAndSpells.style.display = "block";
  const actionsLength = upgradesAndSpells.upgrades.length + upgradesAndSpells.spells.length
  renderUpgradeAndSpellButtons(players[currentActionCounter])
}

function initPlayers(upgradesAndSpells) {
  const upgradeNames = upgradesAndSpells.upgrades
  const spellNames = upgradesAndSpells.spells

  for (const player of players) {
    for (const upgradeName of upgradeNames) {
      player.actions.push({name: upgradeName, used: false})
    }
    for (const spellName of spellNames) {
      player.actions.push({name: spellName, used: false})
    }
  }
}

async function beginSimulation() {
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
    symbols = json.symbols;
  }

  await activateGame(gameId)
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

function restartGame() {
  startGame();
}

function endGame() {
  isRunning = false;
  cancelAnimationFrame(gameLoopId);
  gameScreen.style.display = "none";
  gameOverScreen.style.display = "block";
}

function renderUpgradeAndSpellButtons(player) {
    const hudChooseUpgradesAndSpells = document.getElementById("hudChooseUpgradesAndSpells")
    hudChooseUpgradesAndSpells.innerHTML = ""
    const currentPlayerText = document.createElement("p")
    currentPlayerText.textContent = `current player: ${player.name}`
    hudChooseUpgradesAndSpells.appendChild(currentPlayerText)
    for (const action of player.actions) {
        const btn = document.createElement("button");
        btn.textContent = action.name;
        if (action.used) {
          btn.disabled = true;
        }
        //btn.id = "playerActionButton"
        //btn.classList.add("some-class")
        btn.addEventListener("click", () => handlePlayerActionClick(player, action));
        hudChooseUpgradesAndSpells.appendChild(btn);
    }
}

function handlePlayerActionClick(player, action) {
  console.log(`player ${player.name} clicked action: ${action.name}`);
  player.actions.find(playerAction => playerAction.name === action.name).used = true;
  currentActionCounter += 1;

  
  if (currentActionCounter % 3 === 0 && !player.actions.some(action => action.used === false)) {
    renderBeginSimulationButton()
  } else {
    renderUpgradeAndSpellButtons(players[currentActionCounter % 3])
  }
}

function renderBeginSimulationButton() {
  hudChooseUpgradesAndSpells.style.display = "none";
  hudBeginSimulation.style.display = "block"
}