let timeLeft = 60; // seconds
const canvas = document.getElementById("board");
const ctx = canvas.getContext("2d");

// UI elements
const colorEl = document.getElementById("color");
const sizeEl = document.getElementById("size");
const sizeVal = document.getElementById("sizeVal");
const btnDraw = document.getElementById("modeDraw");
const btnErase = document.getElementById("modeErase");
const timer=document.getElementById("gameTimer")
const btnClear = document.getElementById("clear");

// State
let mode = "draw"; // 'draw' | 'erase'
let drawing = false;
let strokes = []; // { mode, color, size, points: [{x,y}] }
let redoStack = [];
let current = null;

// Device pixel ratio scaling for crisp lines
function resizeCanvas() {
  const dpr = Math.max(1, window.devicePixelRatio || 1);
  const { clientWidth, clientHeight } = canvas;
  if (
    canvas.width !== clientWidth * dpr ||
    canvas.height !== clientHeight * dpr
  ) {
    canvas.width = clientWidth * dpr;
    canvas.height = clientHeight * dpr;
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0); // scale drawing ops to CSS pixels
    redraw();
  }
}

// Initialize sizeVal text
sizeVal.textContent = sizeEl.value;
sizeEl.addEventListener("input", () => (sizeVal.textContent = sizeEl.value));

// Mode toggles
function setMode(next) {
  mode = next;
  const isDraw = mode === "draw";
  btnDraw.classList.toggle("primary", isDraw);
  btnDraw.classList.toggle("ghost", !isDraw);
  btnErase.classList.toggle("primary", !isDraw);
  btnErase.classList.toggle("ghost", isDraw);
  btnDraw.setAttribute("aria-pressed", String(isDraw));
  btnErase.setAttribute("aria-pressed", String(!isDraw));
}
btnDraw.addEventListener("click", () => setMode("draw"));
btnErase.addEventListener("click", () => setMode("erase"));

// timer / clear
function updateTimer() {
        let minutes = Math.floor(timeLeft / 60);
        let seconds = timeLeft % 60;
        
        timer.textContent = 
            `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;

        if (timeLeft > 0) {
            timeLeft--;
        } else {
            clearInterval(timerInterval);
            document.getElementById("gameTimer").textContent = "⏱ Time's Up!";
        }
    }

    let timerInterval = setInterval(updateTimer, 1000);
    updateTimer(); // initial call
function clearBoard() {
  strokes = [];
  redoStack = [];
  current = null;
  drawing = false;
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  updateUndoRedoButtons();
}

btnClear.addEventListener("click", clearBoard);

// Pointer drawing
function getPos(evt) {
  const rect = canvas.getBoundingClientRect();
  return {
    x: evt.clientX - rect.left,
    y: evt.clientY - rect.top,
  };
}

function startStroke(evt) {
  drawing = true;
  redoStack = []; // break redo chain on new input
  current = {
    mode,
    color: colorEl.value,
    size: Number(sizeEl.value),
    points: [],
  };
  addPoint(evt);
}

function addPoint(evt) {
  if (!drawing) return;
  const p = getPos(evt);
  current.points.push(p);
  drawLastSegment();
}

function endStroke() {
  if (!drawing) return;
  drawing = false;
  if (current && current.points.length > 0) {
    strokes.push(current);
  }
  current = null;
  // updateUndoRedoButtons();
}

function drawLastSegment() {
  const s = current;
  const pts = s.points;
  const n = pts.length;
  if (n < 2) return;
  ctx.save();
  ctx.lineCap = "round";
  ctx.lineJoin = "round";
  ctx.lineWidth = s.size;
  if (s.mode === "erase") {
    ctx.globalCompositeOperation = "destination-out";
    ctx.strokeStyle = "rgba(0,0,0,1)";
  } else {
    ctx.globalCompositeOperation = "source-over";
    ctx.strokeStyle = s.color;
  }
  ctx.beginPath();
  ctx.moveTo(pts[n - 2].x, pts[n - 2].y);
  ctx.lineTo(pts[n - 1].x, pts[n - 1].y);
  ctx.stroke();
  ctx.restore();
}

// function redraw() {
//   ctx.clearRect(0, 0, canvas.width, canvas.height);
//   ctx.save();
//   for (const s of strokes) {
//     if (s.points.length < 2) continue;
//     ctx.lineCap = 'round';
//     ctx.lineJoin = 'round';
//     ctx.lineWidth = s.size;
//     if (s.mode === 'erase') {
//       ctx.globalCompositeOperation = 'destination-out';
//       ctx.strokeStyle = 'rgba(0,0,0,1)';
//     } else {
//       ctx.globalCompositeOperation = 'source-over';
//       ctx.strokeStyle = s.color;
//     }
//     ctx.beginPath();
//     ctx.moveTo(s.points[0].x, s.points[0].y);
//     for (let i = 1; i < s.points.length; i++) {
//       ctx.lineTo(s.points[i].x, s.points[i].y);
//     }
//     ctx.stroke();
//   }
//   ctx.restore();
// }

// Pointer events
canvas.addEventListener("pointerdown", (e) => {
  canvas.setPointerCapture(e.pointerId);
  startStroke(e);
});
canvas.addEventListener("pointermove", addPoint);
canvas.addEventListener("pointerup", endStroke);
canvas.addEventListener("pointercancel", endStroke);
canvas.addEventListener("pointerleave", endStroke);

// // Keyboard shortcuts
// window.addEventListener('keydown', (e) => {
//   const z = e.key.toLowerCase() === 'z';
//   if (e.ctrlKey && z && !e.shiftKey) { e.preventDefault(); undo(); }
//   if (e.ctrlKey && z && e.shiftKey) { e.preventDefault(); redo(); }
//   if (e.key.toLowerCase() === 'e') setMode('erase');
//   if (e.key.toLowerCase() === 'b') setMode('draw');
// });

// Resize handling
const ro = new ResizeObserver(resizeCanvas);
ro.observe(canvas);
window.addEventListener("orientationchange", resizeCanvas);
window.addEventListener("load", resizeCanvas);

// Initial state
updateUndoRedoButtons();
setMode("draw");
