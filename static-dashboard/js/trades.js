(function () {

    const table = document.getElementById("trades-table");
    const tbody = document.getElementById("trades-tbody");

    let rows = [];

    function renderRows() {

        tbody.innerHTML = rows.map(row => `
            <tr>
                <td>${row.tradeRef}</td>
                <td>${row.symbol}</td>
                <td>${row.quantity}</td>
                <td>${row.price}</td>
                <td>${row.status}</td>
            </tr>
        `).join("");

    }

    // -----------------------------
    // Sortable Columns
    // -----------------------------

    table.querySelectorAll("thead th").forEach(th => {

        th.addEventListener("click", (event) => {

            if (event.target.classList.contains("resize-handle")) {
                return;
            }

            const column = th.dataset.col;
            const type = th.dataset.type || "string";

            const direction =
                th.getAttribute("aria-sort") === "ascending"
                    ? "descending"
                    : "ascending";

            table.querySelectorAll("thead th")
                .forEach(header => header.removeAttribute("aria-sort"));

            th.setAttribute("aria-sort", direction);

            const multiplier =
                direction === "ascending"
                    ? 1
                    : -1;

            rows.sort((a, b) => {

                const valueA = a[column];
                const valueB = b[column];

                if (type === "number") {
                    return (Number(valueA) - Number(valueB)) * multiplier;
                }

                return String(valueA)
                    .localeCompare(String(valueB))
                    * multiplier;

            });

            renderRows();

        });

    });

    // -----------------------------
    // Resizable Columns
    // -----------------------------

    table.querySelectorAll(".resize-handle")
        .forEach(handle => {

            handle.addEventListener("mousedown", (event) => {

                event.preventDefault();

                const th = handle.closest("th");

                const startX = event.clientX;
                const startWidth = th.offsetWidth;

                function onMove(e) {

                    th.style.width =
                        startWidth + (e.clientX - startX) + "px";

                }

                function onUp() {

                    document.removeEventListener("mousemove", onMove);
                    document.removeEventListener("mouseup", onUp);

                }

                document.addEventListener("mousemove", onMove);
                document.addEventListener("mouseup", onUp);

            });

        });

    // -----------------------------
    // Load Trades
    // -----------------------------

    fetch("http://localhost:8080/api/v1/trades?size=200")
        .then(response => response.json())
        .then(data => {

            rows = data.content || data;

            renderRows();

        });

})();