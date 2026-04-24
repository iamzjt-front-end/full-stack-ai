package com.waylau.rednote.service.impl;

import com.waylau.rednote.common.StringUtil;
import com.waylau.rednote.dto.NoteEditDto;
import com.waylau.rednote.dto.NotePublishDto;
import com.waylau.rednote.entity.Note;
import com.waylau.rednote.entity.User;
import com.waylau.rednote.exception.NoteNotFoundException;
import com.waylau.rednote.repository.NoteRepository;
import com.waylau.rednote.service.FileStorageService;
import com.waylau.rednote.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * NoteServiceImpl 笔记服务
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/18
 **/
@Service
public class NoteServiceImpl implements NoteService {
    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Transactional
    @Override
    public Note createNote(NotePublishDto notePublishDto, User author) {
        Note note = new Note();

        note.setTitle(notePublishDto.getTitle());
        note.setContent(notePublishDto.getContent());
        note.setCategory(notePublishDto.getCategory());
        note.setAuthor(author);

        // 话题字符串转为List
        note.setTopics(StringUtil.splitToList(notePublishDto.getTopics(), " "));

        // 处理图片上传
        List<MultipartFile> images = notePublishDto.getImages();
        if (images != null) {
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String fileName = image.getOriginalFilename();
                    String fileUrl = fileStorageService.saveFile(image, fileName);
                    note.getImages().add(fileUrl);
                }

            }
        }

        return noteRepository.save(note);
    }

    @Override
    public Page<Note> getNotesByUser(Long userId, int page, int size) {
        // 分页查询的笔记列表结果按照创建时间降序排序
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createAt"));
        return noteRepository.findByAuthorUserId(userId, pageable);
    }

    @Override
    public Optional<Note> findNoteById(Long noteId) {
        return noteRepository.findByNoteId(noteId);
    }

    @Override
    public void updateNote(Note note, NoteEditDto noteEditDto) {
        // 将NoteEditDto对象的信息更新到Note对象中
        note.setTitle(noteEditDto.getTitle());
        note.setContent(noteEditDto.getContent());
        note.setCategory(noteEditDto.getCategory());

        // 确保使用可变的集合实现
        /*note.setTopics(StringUtil.splitToList(noteEditDto.getTopics(), " "));*/
        note.setTopics(new ArrayList<>(StringUtil.splitToList(noteEditDto.getTopics(), " ")));

        // 保存更新
        noteRepository.save(note);
    }

    @Override
    @Transactional
    public void deleteNote(Note note) {
        // 注意：先删除数据库数据再删图片文件。以防止删除文件异常时，方便回滚数据库数据

        // 先删除数据库数据
        noteRepository.delete(note);

        // 再删图片文件
        List<String> images = note.getImages();
        for (String image : images) {
            fileStorageService.deleteFile(image);
        }
    }

    @Override
    public boolean isAuthor(Long noteId, String username) {
        Optional<Note> optionalNote = noteRepository.findByNoteId(noteId);
        if (!optionalNote.isPresent()) {
            throw new NoteNotFoundException("");
        }

        return username.equals(optionalNote.get().getAuthor().getUsername());
    }

    @Override
    public Page<Note> getNotesByPage(int page, int pageSize, String category) {
        // 构造Pageable对象，按照创建时间倒序排序
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("createAt").descending());

        if (category != null && !category.isEmpty()) {
            return noteRepository.findByCategory(category, pageable);
        }

        return noteRepository.findAll(pageable);
    }

    @Override
    public Page<Note> getNotesByPageAndQuery(int page, int pageSize, String category, String query) {
        // 构造Pageable对象，按照创建时间倒序排序
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("createAt").descending());

        if (category != null && !category.isEmpty() && query != null && !query.isEmpty()) {
            return noteRepository.findByCategoryAndTopicsContaining(category, query, pageable);
        } else if (query != null && !query.isEmpty()) {
            return noteRepository.findByTopicsContaining(query, pageable);
        } else {
            return noteRepository.findAll(pageable);
        }
    }

    @Override
    public long countNotes() {
        return noteRepository.count();
    }
}
