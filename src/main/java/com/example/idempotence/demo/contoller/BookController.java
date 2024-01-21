package com.example.idempotence.demo.contoller;

import com.example.idempotence.demo.exception.ConflictException;
import com.example.idempotence.demo.exception.NotFoundException;
import com.example.idempotence.demo.model.Book;
import com.example.idempotence.demo.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Optional;


@RestController
@RequestMapping("/v1/api/books")
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    /*
    The getBook method implements the GET operation and returns the book with the specified ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBook(@PathVariable Long id) {
        // Implementation for GET
        Optional<Book> optionalBook = bookRepository.findById(id);
        return optionalBook.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /*
    The updateBook method implements the PUT operation and updates the existing book if it exists,
    checking for an Etag match to ensure idempotence.
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateBook(
            @PathVariable Long id,
            @RequestHeader(value = "If-Match", required = false) String ifMatchHeader,
            @RequestBody Book updatedBook) {
        // Implementation for PUT
        Optional<Book> existingBook = bookRepository.findById(id);
        if (existingBook.isPresent()) {
            if (ifMatchHeader != null && !Objects.equals(ifMatchHeader, calculateEtag(existingBook.get()))) {
                throw new ConflictException("Etag mismatch");
            }

            // Update the existing book
            existingBook.get().setTitle(updatedBook.getTitle());
            existingBook.get().setAuthor(updatedBook.getAuthor());
            bookRepository.save(existingBook.get());

            return ResponseEntity.ok("Book updated successfully");
        } else {
            throw new NotFoundException("Book not found");
        }
    }

    /*
    The deleteBook method implements the DELETE operation and deletes the book if it exists
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable Long id) {
        // Implementation for DELETE
        Optional<Book> existingBook = bookRepository.findById(id);
        if (existingBook.isPresent()) {
            // Delete the existing book
            bookRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            throw new NotFoundException("Book not found");
        }
    }

    // Utility method to calculate Etag (example)
    private String calculateEtag(Book book) {
        return Integer.toHexString(book.hashCode());
    }
}

