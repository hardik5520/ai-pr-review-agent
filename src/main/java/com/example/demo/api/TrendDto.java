package com.example.demo.api;

/**
 * Aggregated trend data for one contributor — used to draw the bar chart on the Trends page.
 * Each instance represents one bar: the author's name on the X axis,
 * how many PRs they opened on the Y axis.
 *
 * This is intentionally simple — the frontend can sort/filter as needed.
 */
public record TrendDto(

        // GitHub username of the contributor
        String author,

        // Total number of pull requests they have opened (that we've reviewed)
        Long prCount
) {}
