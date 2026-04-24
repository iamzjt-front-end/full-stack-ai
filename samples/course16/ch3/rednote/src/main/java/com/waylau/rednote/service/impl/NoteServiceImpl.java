package com.waylau.rednote.service.impl;

import com.waylau.rednote.common.StringUtil;
import com.waylau.rednote.dto.NoteBrowseCountDto;
import com.waylau.rednote.dto.NoteBrowseTimeDto;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * NoteServiceImpl 笔记服务
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/18
 **/
@Service
public class NoteServiceImpl implements NoteService {

    private static final String NOTE_COUNT_KEY = "rednote:note:note_count";

    private static final String BROWSE_COUNT_KEY = "rednote:note:browse_count";

    private static final String BROWSE_TIME_KEY = "rednote:note:browse_time";

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Transactional
    @Override
    public Note createNote(NotePublishDto notePublishDto, User author) {
        // 创建笔记对象时在Redis递增笔记总数
        redisTemplate.opsForValue().increment(NOTE_COUNT_KEY);

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
        // 在删除笔记时在Redis递减笔记总数
        redisTemplate.opsForValue().decrement(NOTE_COUNT_KEY);

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
        // 从Redis中获取笔记总数
        String noteCountFromRedis = redisTemplate.opsForValue().get(NOTE_COUNT_KEY);

        // 判定Redis是否命中
        if (noteCountFromRedis != null) {
            // 命中，则直接返回
            return Long.parseLong(noteCountFromRedis);
        } else {
            // 未命中，从数据库中查询
            long noteCountFromDb = noteRepository.count();
            redisTemplate.opsForValue().set(NOTE_COUNT_KEY, String.valueOf(noteCountFromDb));

            return noteCountFromDb;
        }

        /*return noteRepository.count();*/
    }

    @Override
    public void incrementBrowseCount(Long noteId) {
        redisTemplate.opsForZSet().incrementScore(BROWSE_COUNT_KEY, noteId.toString(), 1);
    }

    @Override
    public void incrementBrowseTime(Long noteId, long browseTime) {
        redisTemplate.opsForZSet().incrementScore(BROWSE_TIME_KEY, noteId.toString(), browseTime);
    }

    @Override
    public List<NoteBrowseCountDto> getNotesByBrowseCount(int page, int size) {
        // 获取Redis中指定区间的笔记浏览次数, 按照笔记浏访问量降序排序
        Set<ZSetOperations.TypedTuple<String>> typedTuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(BROWSE_COUNT_KEY, (page - 1) * size, page * size - 1);

        // 转为DTO列表
        List<NoteBrowseCountDto> noteBrowseCountDtos = typedTuples.stream()
                .map(tuple -> {
                    NoteBrowseCountDto noteBrowseCountDto = new NoteBrowseCountDto();
                    long noteId = Long.parseLong(tuple.getValue());
                    noteBrowseCountDto.setNoteId(noteId);
                    noteBrowseCountDto.setTitle(noteRepository.findByNoteId(noteId).get().getTitle());
                    noteBrowseCountDto.setBrowseCount(tuple.getScore().longValue());
                    return noteBrowseCountDto;
                }).collect(Collectors.toUnmodifiableList());

        return noteBrowseCountDtos;
    }

    @Override
    public List<NoteBrowseTimeDto> getNotesByBrowseTime(int page, int size) {
        // 获取Redis中指定区间的笔记浏览次数, 按照笔记浏访问量降序排序
        Set<ZSetOperations.TypedTuple<String>> typedTuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(BROWSE_TIME_KEY, (page - 1) * size, page * size - 1);

        // 转为DTO列表
        List<NoteBrowseTimeDto> noteBrowseTimeDtos = typedTuples.stream()
                .map(tuple -> {
                    NoteBrowseTimeDto noteBrowseTimeDto = new NoteBrowseTimeDto();
                    long noteId = Long.parseLong(tuple.getValue());
                    noteBrowseTimeDto.setNoteId(noteId);
                    noteBrowseTimeDto.setTitle(noteRepository.findByNoteId(noteId).get().getTitle());
                    noteBrowseTimeDto.setBrowseTime(tuple.getScore().longValue());
                    return noteBrowseTimeDto;
                }).collect(Collectors.toUnmodifiableList());

        return noteBrowseTimeDtos;
    }
}
