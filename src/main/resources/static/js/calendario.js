const calendar = document.getElementById("calendar");
const monthYear = document.getElementById("month-year");
const prevBtn = document.getElementById("prev");
const nextBtn = document.getElementById("next");

let currentDate = new Date();
let selectedDay = null;

const meses = [
  "enero", "febrero", "marzo", "abril", "mayo", "junio",
  "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
];

// 👇 días disponibles y no disponibles
const dias = {
  10: "disponible",
  18: "disponible",
  25: "no-disponible"
};

function generarCalendario(date) {
  calendar.innerHTML = "";

  const year = date.getFullYear();
  const month = date.getMonth();

  monthYear.innerText = `${meses[month]} ${year}`;

  const firstDay = new Date(year, month, 1).getDay();
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const start = firstDay === 0 ? 6 : firstDay - 1;

  // Espacios vacíos
  for (let i = 0; i < start; i++) {
    const empty = document.createElement("div");
    empty.classList.add("day", "empty");
    calendar.appendChild(empty);
  }

  // ✅ DÍAS DEL MES
  for (let i = 1; i <= daysInMonth; i++) {
    const dia = document.createElement("div");
    dia.classList.add("day");
    dia.innerText = i;

    // Si el día está en la lista, agregamos la clase correspondiente
    if (dias[i]) {
      dia.classList.add(dias[i]); // "disponible" o "no-disponible"
    }
    // ❌ Si no está definido, lo dejamos sin clase extra (blanco)

    calendar.appendChild(dia);
  }
}

// ✅ Evento de clic en el calendario
calendar.addEventListener("click", (ev) => {
  const dia = ev.target.closest('.day');
  if (!dia) return;
  if (dia.classList.contains('empty')) return;
  if (dia.classList.contains('no-disponible')) return;

  // Quitar selección anterior
  const prev = calendar.querySelector('.day.selected');
  if (prev) prev.classList.remove('selected');

  // Marcar el nuevo día seleccionado
  dia.classList.add('selected');

  // Guardar la fecha seleccionada
  const dayNum = parseInt(dia.innerText);
  selectedDay = new Date(currentDate.getFullYear(), currentDate.getMonth(), dayNum);
  console.log("📅 Día seleccionado:", selectedDay.toDateString());
});

// Botones navegación
prevBtn.addEventListener("click", () => {
  currentDate.setMonth(currentDate.getMonth() - 1);
  generarCalendario(currentDate);
});

nextBtn.addEventListener("click", () => {
  currentDate.setMonth(currentDate.getMonth() + 1);
  generarCalendario(currentDate);
});

// Inicializar calendario
generarCalendario(currentDate);
