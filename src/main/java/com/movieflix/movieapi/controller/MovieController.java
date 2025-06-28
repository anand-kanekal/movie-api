package com.movieflix.movieapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieflix.movieapi.dto.MovieDto;
import com.movieflix.movieapi.dto.MoviePageResponse;
import com.movieflix.movieapi.exception.FileNotAddedException;
import com.movieflix.movieapi.service.MovieService;
import com.movieflix.movieapi.utils.AppConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public ResponseEntity<MovieDto> addMovie(@RequestPart MultipartFile file, @RequestPart String movieDtoRequest) throws IOException {
        if (file.isEmpty()) {
            throw new FileNotAddedException("File not added");
        }

        MovieDto movieDto = convertToMovieDto(movieDtoRequest);
        MovieDto savedMovie = movieService.addMovie(movieDto, file);
        return new ResponseEntity<>(savedMovie, HttpStatus.CREATED);
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<MovieDto> getMovie(@PathVariable Integer movieId) {
        MovieDto movieDto = movieService.getMovie(movieId);
        return new ResponseEntity<>(movieDto, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<MovieDto>> getAllMovies() {
        List<MovieDto> movies = movieService.getAllMovies();
        return new ResponseEntity<>(movies, HttpStatus.OK);
    }

    @GetMapping("/pagination")
    public ResponseEntity<MoviePageResponse> getAllMoviesWithPagination(@RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) Integer pageNumber,
                                                                        @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) Integer pageSize) {
        MoviePageResponse allMoviesWithPagination = movieService.getAllMoviesWithPagination(pageNumber, pageSize);
        return new ResponseEntity<>(allMoviesWithPagination, HttpStatus.OK);
    }

    @GetMapping("/pagination/sort")
    public ResponseEntity<MoviePageResponse> getAllMoviesWithPaginationAndSorting(@RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) Integer pageNumber,
                                                                                  @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) Integer pageSize,
                                                                                  @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
                                                                                  @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDirection) {
        MoviePageResponse allMoviesWithPaginationAndSorting = movieService.getAllMoviesWithPaginationAndSorting(pageNumber, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(allMoviesWithPaginationAndSorting, HttpStatus.OK);
    }

    @PutMapping("/{movieId}")
    public ResponseEntity<MovieDto> updateMovie(@PathVariable Integer movieId, @RequestPart MultipartFile file, @RequestPart String movieDtoRequest) throws IOException {
        if (file.isEmpty()) {
            file = null;
        }

        MovieDto movieDto = convertToMovieDto(movieDtoRequest);
        MovieDto updatedMovie = movieService.updateMovie(movieId, movieDto, file);
        return new ResponseEntity<>(updatedMovie, HttpStatus.OK);
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<String> deleteMovie(@PathVariable Integer movieId) throws IOException {
        String message = movieService.deleteMovie(movieId);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    private MovieDto convertToMovieDto(String movieDtoRequest) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(movieDtoRequest, MovieDto.class);
    }
}