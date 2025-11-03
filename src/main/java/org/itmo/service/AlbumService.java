package org.itmo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate; 

import jakarta.persistence.EntityNotFoundException;
import org.itmo.model.Album;
import org.itmo.repository.AlbumRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@Transactional
public class AlbumService {
    private final AlbumRepository albumRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public AlbumService(AlbumRepository albumRepository, SimpMessagingTemplate messagingTemplate) {
        this.albumRepository = albumRepository;
        this.messagingTemplate = messagingTemplate;
    }

    private void notifyClients() {

        messagingTemplate.convertAndSend("/topic/bands/updates", "ALBUM_UPDATED");
    }

    public Page<Album> getAll(Pageable pageable) {
        return albumRepository.findAll(pageable);
    }

    public Album getById(Long id) {
        return albumRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Album not found: " + id));
    }

    public Album create(Album album) {
        Album createdAlbum = albumRepository.save(album);
        notifyClients();
        return createdAlbum;
    }

    public Album update(Long id, Album patch) {
        Album existing = getById(id);


        if (patch.getName() != null) existing.setName(patch.getName());
        if (patch.getTracks() > 0) existing.setTracks(patch.getTracks());
        if (patch.getLength() > 0) existing.setLength(patch.getLength());

        if (patch.getSales() != null) {

            existing.setSales(patch.getSales());
        }

        Album updatedAlbum = albumRepository.save(existing);
        notifyClients();
        return updatedAlbum;
    }

    public void delete(Long id) {

        albumRepository.deleteById(id);
        notifyClients();
    }
}