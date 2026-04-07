package com.example.studyai.service;

import com.example.studyai.dto.SubjectDTO;
import com.example.studyai.model.Subject;
import com.example.studyai.model.User;
import com.example.studyai.repository.SubjectRepository;
import com.example.studyai.repository.NoteRepository;
import com.example.studyai.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SubjectService {

    private static final Logger logger = LoggerFactory.getLogger(SubjectService.class);

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Transactional
    public Subject createSubject(User user, String name, String description) {
        Subject subject = new Subject();
        subject.setUser(user);
        subject.setName(name);
        subject.setDescription(description);
        return subjectRepository.save(subject);
    }

    public Optional<Subject> getSubjectById(UUID id) {
        return subjectRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public SubjectDTO getSubjectByIdDTO(UUID id) {
        // Convert within transaction to avoid LazyInitializationException
        Optional<Subject> subject = subjectRepository.findById(id);
        return subject.map(this::convertToDTO).orElse(null);
    }

    public List<Subject> getUserSubjects(UUID userId) {
        return subjectRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<SubjectDTO> getUserSubjectsDTO(UUID userId) {
        // Use eager-loading query to avoid N+1 queries and PgBouncer conflicts
        // Eagerly loads User relationship to prevent lazy-loading queries within the
        // transaction
        return convertToDTOList(subjectRepository.findByUserIdWithUserEager(userId));
    }

    @Transactional
    public Subject updateSubject(UUID id, String name, String description) {
        Optional<Subject> subject = subjectRepository.findById(id);
        if (subject.isPresent()) {
            Subject s = subject.get();
            s.setName(name);
            s.setDescription(description);
            return subjectRepository.save(s);
        }
        throw new RuntimeException("Subject not found");
    }

    @Transactional
    public void deleteSubject(UUID id) {
        subjectRepository.deleteById(id);
    }

    public SubjectDTO convertToDTO(Subject subject) {
        // Count notes in this subject
        Integer notesCount = noteRepository.findBySubjectId(subject.getId()).size();

        // Count quizzes that have notes from this subject
        List<UUID> noteIdsInSubject = noteRepository.findBySubjectId(subject.getId())
                .stream()
                .map(note -> note.getId())
                .collect(Collectors.toList());

        Integer quizzesCount = 0;
        if (!noteIdsInSubject.isEmpty()) {
            quizzesCount = (int) quizRepository.findByUserId(subject.getUser().getId())
                    .stream()
                    .filter(quiz -> quiz.getQuizNotes() != null &&
                            quiz.getQuizNotes().stream()
                                    .anyMatch(qn -> noteIdsInSubject.contains(qn.getNote().getId())))
                    .count();
        }

        return new SubjectDTO(subject.getId(), subject.getUser().getId(), subject.getName(),
                subject.getDescription(), subject.getCreatedAt(), subject.getUpdatedAt(),
                notesCount, quizzesCount);
    }

    public List<SubjectDTO> convertToDTOList(List<Subject> subjects) {
        return subjects.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
}
