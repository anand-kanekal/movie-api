package com.movieflix.movieapi.service.impl;

import com.movieflix.movieapi.dto.MovieDto;
import com.movieflix.movieapi.dto.MoviePageResponse;
import com.movieflix.movieapi.entity.Movie;
import com.movieflix.movieapi.exception.FileExistsException;
import com.movieflix.movieapi.exception.MovieNotFoundException;
import com.movieflix.movieapi.repository.MovieRepository;
import com.movieflix.movieapi.service.FileService;
import com.movieflix.movieapi.service.MovieService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
public class MovieServiceImpl implements MovieService {

    @Value("${project.poster}")
    private String fileUploadPath;

    @Value("${base.url}")
    private String baseUrl;

    private final MovieRepository movieRepository;

    private final FileService fileService;

    public MovieServiceImpl(MovieRepository movieRepository, FileService fileService) {
        this.movieRepository = movieRepository;
        this.fileService = fileService;
    }

    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException {
        // Upload the file
        String fileLocation = fileUploadPath + File.separator + file.getOriginalFilename();

        if (Files.exists(Paths.get(fileLocation))) {
            throw new FileExistsException("File already exists! Please upload a different file");
        }

        String uploadedFileName = fileService.uploadFile(fileUploadPath, file);

        movieDto.setPoster(uploadedFileName);

        Movie movie = new Movie(null,
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster());

        Movie savedMovie = movieRepository.save(movie);

        String posterUrl = baseUrl + "/api/file" + "/" + uploadedFileName;

        return new MovieDto(savedMovie.getMovieId(),
                savedMovie.getTitle(),
                savedMovie.getDirector(),
                savedMovie.getStudio(),
                savedMovie.getMovieCast(),
                savedMovie.getReleaseYear(),
                savedMovie.getPoster(),
                posterUrl);
    }

    @Override
    public MovieDto getMovie(Integer movieId) {
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException("Movie not found with id: " + movieId));

        String posterUrl = baseUrl + "/api/file" + "/" + movie.getPoster();

        return new MovieDto(movie.getMovieId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getStudio(),
                movie.getMovieCast(),
                movie.getReleaseYear(),
                movie.getPoster(),
                posterUrl);
    }

    @Override
    public List<MovieDto> getAllMovies() {
        List<Movie> movies = movieRepository.findAll();

        return movies.stream().map(movie -> {
            String posterUrl = baseUrl + "/api/file" + "/" + movie.getPoster();
            return new MovieDto(movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl);
        }).toList();
    }

    @Override
    public MoviePageResponse getAllMoviesWithPagination(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Movie> moviePages = movieRepository.findAll(pageable);
        List<Movie> movies = moviePages.getContent();

        List<MovieDto> movieDtos = movies.stream().map(movie -> {
            String posterUrl = baseUrl + "/api/file" + "/" + movie.getPoster();
            return new MovieDto(movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl);
        }).toList();

        return new MoviePageResponse(movieDtos, pageNumber, pageSize, moviePages.getTotalElements(), moviePages.getTotalPages(), moviePages.isLast());
    }

    @Override
    public MoviePageResponse getAllMoviesWithPaginationAndSorting(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection) {
        Sort sort = Sort.by(sortBy);
        if (sortDirection.equalsIgnoreCase("asc")) {
            sort = sort.ascending();
        } else if (sortDirection.equalsIgnoreCase("dsc")) {
            sort = sort.descending();
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Movie> moviePages = movieRepository.findAll(pageable);
        List<Movie> movies = moviePages.getContent();

        List<MovieDto> movieDtos = movies.stream().map(movie -> {
            String posterUrl = baseUrl + "/api/file" + "/" + movie.getPoster();
            return new MovieDto(movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl);
        }).toList();

        return new MoviePageResponse(movieDtos, pageNumber, pageSize, moviePages.getTotalElements(), moviePages.getTotalPages(), moviePages.isLast());
    }

    @Override
    public MovieDto updateMovie(Integer movieId, MovieDto movieDto, MultipartFile file) throws IOException {
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException("Movie not found with id: " + movieId));

        String fileName = movie.getPoster();

        if (file != null) {
            Files.deleteIfExists(Paths.get(fileUploadPath + File.separator + fileName));
            fileName = fileService.uploadFile(fileUploadPath, file);
        }

        movieDto.setPoster(fileName);

        Movie updatedMovie = new Movie(movie.getMovieId(),
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster());

        Movie savedMovie = movieRepository.save(updatedMovie);

        return new MovieDto(savedMovie.getMovieId(),
                savedMovie.getTitle(),
                savedMovie.getDirector(),
                savedMovie.getStudio(),
                savedMovie.getMovieCast(),
                savedMovie.getReleaseYear(),
                savedMovie.getPoster(),
                baseUrl + "/api/file" + "/" + savedMovie.getPoster());
    }

    @Override
    public String deleteMovie(Integer movieId) throws IOException {
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException("Movie not found with id: " + movieId));
        String id = movie.getMovieId().toString();
        Files.deleteIfExists(Paths.get(fileUploadPath + File.separator + movie.getPoster()));
        movieRepository.deleteById(movie.getMovieId());
        return "Movie deleted successfully with id: " + id;
    }
}
