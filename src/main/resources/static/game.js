import { activateGame, getInitialGameState, getNewGameId } from "./api.js";

//references to screen elements
const mainMenu = document.getElementById("mainMenu");
const gameScreen = document.getElementById("gameScreen");
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
