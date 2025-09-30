const calendar = document.getElementById("calendar");
const monthYear = document.getElementById("month-year");
const prevBtn = document.getElementById("prev");
const nextBtn = document.getElementById("next");

let currentDate = new Date();

const meses = [
  "enero", "febrero", "marzo", "abril", "mayo", "junio",
  "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
];

// Ejemplo de días con estados
const dias = {

  10: "disponible",
  18: "disponible",
  25: "no-disponible"
};

function generarCalendario(date) {
  calendar.innerHTML = "";

  const year = date.getFullYear();
  const month = date.getMonth();

  // título mes y año
  monthYear.innerText = `${meses[month]} ${year}`;

  // primer día del mes y cantidad de días
  const firstDay = new Date(year, month, 1).getDay(); 
  const daysInMonth = new Date(year, month + 1, 0).getDate();

  // Ajuste: en JS, domingo = 0, lunes = 1...
  const start = firstDay === 0 ? 6 : firstDay - 1;

  // huecos antes del día 1
  for (let i = 0; i < start; i++) {
    const empty = document.createElement("div");
    empty.classList.add("day", "empty");
    calendar.appendChild(empty);
  }

  // días del mes
  for (let i = 1; i <= daysInMonth; i++) {
    const dia = document.createElement("div");
    dia.classList.add("day");
    dia.innerText = i;

    // marcar estado según ejemplo
    if (dias[i]) {
      dia.classList.add(dias[i]);
    }

    calendar.appendChild(dia);
  }
}

// Botones navegación
prevBtn.addEventListener("click", () => {
  currentDate.setMonth(currentDate.getMonth() - 1);
  generarCalendario(currentDate);
});

nextBtn.addEventListener("click", () => {
  currentDate.setMonth(currentDate.getMonth() + 1);
  generarCalendario(currentDate);
});

// iniciar
generarCalendario(currentDate);
