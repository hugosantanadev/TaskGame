package com.gasmtask.shared.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.springframework.stereotype.Component;

@Component
public class LocalFileStorage implements FileStorage {

    private final Path root;

    public LocalFileStorage(StorageProperties properties) {
        this.root = Path.of(properties.dir()).toAbsolutePath().normalize();
    }

    @Override
    public void save(String key, byte[] content) {
        Path target = resolve(key);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gravar o arquivo " + key, e);
        }
    }

    @Override
    public byte[] read(String key) {
        try {
            return Files.readAllBytes(resolve(key));
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler o arquivo " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao apagar o arquivo " + key, e);
        }
    }

    @Override
    public void deleteFolder(String prefix) {
        Path folder = resolve(prefix);
        if (!Files.exists(folder)) {
            return;
        }
        try (var paths = Files.walk(folder)) {
            // Do mais fundo para a raiz: arquivos antes das pastas que os contêm
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao apagar a pasta " + prefix, e);
        }
    }

    /** Impede que uma chave como "../../etc/passwd" escape do diretório de armazenamento. */
    private Path resolve(String key) {
        Path path = root.resolve(key).normalize();
        if (!path.startsWith(root)) {
            throw new IllegalArgumentException("Chave fora do diretório de armazenamento: " + key);
        }
        return path;
    }
}
