$(document).ready(function() {
    let searchFrequency = {};
    let watchData = {}; // This will store the scraped data

    // Simulated watch data (replace this with your actual scraped data)
    watchData = {
        "Apple Watch Series 9": "Data for Series 9...",
        "Apple Watch Ultra 2": "Data for Ultra 2...",
        "Apple Watch SE": "Data for SE..."
    };

    $("#searchInput").on("input", function() {
        let input = $(this).val().toLowerCase();
        let suggestions = Object.keys(watchData).filter(watch =>
            watch.toLowerCase().includes(input)
        );

        displaySuggestions(suggestions);
    });

    function displaySuggestions(suggestions) {
        let html = suggestions.map(s => `<div class="suggestion">${s}</div>`).join('');
        $("#suggestions").html(html);

        $(".suggestion").on("click", function() {
            let selected = $(this).text();
            $("#searchInput").val(selected);
            search(selected);
        });
    }

    $("#searchInput").on("keyup", function(e) {
        if (e.key === "Enter") {
            search($(this).val());
        }
    });

    function search(query) {
        // Update search frequency
        searchFrequency[query] = (searchFrequency[query] || 0) + 1;

        // Display results
        let result = watchData[query] || "No data found for this model.";
        $("#results").html(`<h2>${query}</h2><p>${result}</p>`);

        // Clear suggestions
        $("#suggestions").empty();

        // Log search frequency
        console.log("Search Frequencies:", searchFrequency);
    }

    function getSuggestions(input) {
        $.get("/suggestions", { input: input }, function(data) {
            displaySuggestions(data);
        });
    }

    function search(query) {
        $.get("/search", { query: query }, function(data) {
            $("#results").html(data);
        });
    }
});

