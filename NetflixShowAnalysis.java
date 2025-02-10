import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.*;
import java.awt.*;

public class NetflixShowAnalysis {
    public static void main(String[] args) {
        String filePath = "netflix_titles.csv";  // Make sure the dataset file is in the same directory
        ArrayList<NetflixShow> shows = new ArrayList<>();

        // Read the CSV file
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine();  // Skip the header line
            while ((line = br.readLine()) != null) {
                // Split line while handling commas inside quotes
                String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                // Ensure the row has at least 11 columns to access index 10 safely
                if (values.length > 10) {
                    String title = values[2].replace("\"", "").trim();
                    String type = values[1].replace("\"", "").trim();
                    String listedIn = values[10].replace("\"", "").trim();

                    // Safely parse release year
                    int releaseYear = 0;
                    String releaseYearStr = values[7].replace("\"", "").trim();
                    if (!releaseYearStr.isEmpty()) {
                        try {
                            releaseYear = Integer.parseInt(releaseYearStr);
                        } catch (NumberFormatException e) {
                            releaseYear = 0;
                        }
                    }

                    // Add to the list
                    NetflixShow show = new NetflixShow(title, type, listedIn, releaseYear);
                    shows.add(show);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Analyze Data: Count movies vs TV shows
        int movieCount = 0;
        int tvShowCount = 0;
        ArrayList<String> genres = new ArrayList<>();
        ArrayList<Integer> genreCounts = new ArrayList<>();
        ArrayList<Integer> years = new ArrayList<>();
        ArrayList<Integer> moviesPerYear = new ArrayList<>();
        ArrayList<Integer> tvShowsPerYear = new ArrayList<>();

        for (NetflixShow show : shows) {
            if (show.getType().equalsIgnoreCase("Movie")) {
                movieCount++;
            } else if (show.getType().equalsIgnoreCase("TV Show")) {
                tvShowCount++;
            }

            // Count releases by year
            int releaseYear = show.getReleaseYear();
            if (releaseYear > 0) {
                int index = years.indexOf(releaseYear);
                if (index != -1) {
                    if (show.getType().equalsIgnoreCase("Movie")) {
                        moviesPerYear.set(index, moviesPerYear.get(index) + 1);
                    } else if (show.getType().equalsIgnoreCase("TV Show")) {
                        tvShowsPerYear.set(index, tvShowsPerYear.get(index) + 1);
                    }
                } else {
                    years.add(releaseYear);
                    moviesPerYear.add(show.getType().equalsIgnoreCase("Movie") ? 1 : 0);
                    tvShowsPerYear.add(show.getType().equalsIgnoreCase("TV Show") ? 1 : 0);
                }
            }

            // Analyze Data: Find the most common genre
            String[] showGenres = show.getGenre().split(",");
            for (String genre : showGenres) {
                genre = genre.trim();
                int index = genres.indexOf(genre);
                if (index != -1) {
                    genreCounts.set(index, genreCounts.get(index) + 1);
                } else {
                    genres.add(genre);
                    genreCounts.add(1);
                }
            }
        }

        // Sort the years and corresponding data
        ArrayList<Integer> sortedYears = new ArrayList<>(years);
        Collections.sort(sortedYears);
        ArrayList<Integer> sortedMoviesPerYear = new ArrayList<>();
        ArrayList<Integer> sortedTVShowsPerYear = new ArrayList<>();

        for (int year : sortedYears) {
            int index = years.indexOf(year);
            sortedMoviesPerYear.add(moviesPerYear.get(index));
            sortedTVShowsPerYear.add(tvShowsPerYear.get(index));
        }

        // Find the top 5 genres
        ArrayList<String> topGenres = new ArrayList<>();
        ArrayList<Integer> topGenreCounts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int maxIndex = 0;
            for (int j = 0; j < genreCounts.size(); j++) {
                if (genreCounts.get(j) > genreCounts.get(maxIndex)) {
                    maxIndex = j;
                }
            }
            topGenres.add(genres.get(maxIndex));
            topGenreCounts.add(genreCounts.get(maxIndex));
            genreCounts.set(maxIndex, -1); // Mark this genre as used
        }

        // Display results in the console
        System.out.println("Total Movies: " + movieCount);
        System.out.println("Total TV Shows: " + tvShowCount);
        System.out.println("Top 5 genres:");
        for (int i = 0; i < topGenres.size(); i++) {
            System.out.println(topGenres.get(i) + " (" + topGenreCounts.get(i) + " titles)");
        }

        // Display results graphically
        JFrame frame = new JFrame("Netflix Analysis");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new GridLayout(2, 1));
        frame.add(new PopularityLineChartPanel(sortedYears, sortedMoviesPerYear, sortedTVShowsPerYear));
        frame.add(new GenreBarChartPanel(topGenres, topGenreCounts));
        frame.setVisible(true);
    }
}

class NetflixShow {
    private String title;
    private String type;  // Movie or TV Show
    private String genre; // Derived from the 'listed_in' column
    private int releaseYear;

    public NetflixShow(String title, String type, String listedIn, int releaseYear) {
        this.title = title;
        this.type = type;
        this.genre = listedIn.split(",")[0].trim();  // Use the first listed genre
        this.releaseYear = releaseYear;
    }

    public String getGenre() {
        return genre;
    }

    public String getType() {
        return type;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    @Override
    public String toString() {
        return title + " (" + releaseYear + ") - " + type + " - " + genre;
    }
}

class PopularityLineChartPanel extends JPanel {
    private ArrayList<Integer> years;
    private ArrayList<Integer> moviesPerYear;
    private ArrayList<Integer> tvShowsPerYear;

    public PopularityLineChartPanel(ArrayList<Integer> years, ArrayList<Integer> moviesPerYear, ArrayList<Integer> tvShowsPerYear) {
        this.years = years;
        this.moviesPerYear = moviesPerYear;
        this.tvShowsPerYear = tvShowsPerYear;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();
        int padding = 50;

        int maxMovies = moviesPerYear.stream().max(Integer::compare).orElse(1);
        int maxTVShows = tvShowsPerYear.stream().max(Integer::compare).orElse(1);
        int maxCount = Math.max(maxMovies, maxTVShows);

        int xStep = (width - 2 * padding) / (years.size() - 1);

        g.drawString("Movies (Blue) and TV Shows (Red) Over Time", width / 4, padding / 2);

        // Draw movies line
        g.setColor(Color.BLUE);
        for (int i = 0; i < years.size() - 1; i++) {
            int x1 = padding + i * xStep;
            int y1 = height - padding - (moviesPerYear.get(i) * (height - 2 * padding) / maxCount);
            int x2 = padding + (i + 1) * xStep;
            int y2 = height - padding - (moviesPerYear.get(i + 1) * (height - 2 * padding) / maxCount);
            g.drawLine(x1, y1, x2, y2);
        }

        // Draw TV shows line
        g.setColor(Color.RED);
        for (int i = 0; i < years.size() - 1; i++) {
            int x1 = padding + i * xStep;
            int y1 = height - padding - (tvShowsPerYear.get(i) * (height - 2 * padding) / maxCount);
            int x2 = padding + (i + 1) * xStep;
            int y2 = height - padding - (tvShowsPerYear.get(i + 1) * (height - 2 * padding) / maxCount);
            g.drawLine(x1, y1, x2, y2);
        }

        // Draw year labels
        g.setColor(Color.BLACK);
        for (int i = 0; i < years.size(); i += Math.max(1, years.size() / 10)) {
            int x = padding + i * xStep;
            g.drawString(String.valueOf(years.get(i)), x, height - padding + 20);
        }
    }
}

class GenreBarChartPanel extends JPanel {
    private ArrayList<String> genres;
    private ArrayList<Integer> counts;

    public GenreBarChartPanel(ArrayList<String> genres, ArrayList<Integer> counts) {
        this.genres = genres;
        this.counts = counts;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();
        int padding = 50;
        int barWidth = (width - 2 * padding) / genres.size();

        int maxCount = counts.stream().max(Integer::compare).orElse(1);

        g.drawString("Top 5 Genres", width / 2 - 30, padding / 2);

        for (int i = 0; i < genres.size(); i++) {
            int barHeight = (counts.get(i) * (height - 2 * padding)) / maxCount;
            int x = padding + i * barWidth;
            int y = height - padding - barHeight;

            g.setColor(Color.GRAY);
            g.fillRect(x, y, barWidth - 10, barHeight);

            g.setColor(Color.BLACK);
            g.drawRect(x, y, barWidth - 10, barHeight);
            g.drawString(genres.get(i), x, height - padding + 20);
        }
    }
}
