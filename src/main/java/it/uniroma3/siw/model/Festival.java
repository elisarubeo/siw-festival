package it.uniroma3.siw.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
public class Festival {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Il nome è obbligatorio")
    @Column(nullable = false)
    private String name;

    @Size(max = 2000, message = "La descrizione non può superare i 2000 caratteri")
    @Column(length = 2000)
    private String description;

    @NotBlank(message = "La città è obbligatoria")
    @Column(nullable = false)
    private String city;

    @NotNull(message = "L'anno è obbligatorio")
    @Column(nullable = false)
    private Integer year;

    @NotNull(message = "La data di inizio è obbligatoria")
    @Column(nullable = false)
    private LocalDate startDate;

    @NotNull(message = "La data di fine è obbligatoria")
    @Column(nullable = false)
    private java.time.LocalDate endDate;

    /* lato inverso della ManyToMany: il proprietario e' Movie.festivals */
    @ManyToMany(mappedBy = "festivals")
    private List<Movie> movies = new ArrayList<>();

    /* le proiezioni non esistono senza il festival: cascade + orphanRemoval */
    @OrderBy("date ASC, time ASC")
    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Screening> screenings = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public java.time.LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(java.time.LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }

    public List<Screening> getScreenings() {
        return screenings;
    }

    public void setScreenings(List<Screening> screenings) {
        this.screenings = screenings;
    }

    @Override
    public int hashCode() {
        return 31 + ((id == null) ? 0 : id.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Festival other = (Festival) obj;
        return id != null && id.equals(other.id);
    }

    
}
