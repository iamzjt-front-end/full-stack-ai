export class CommentResponseDto {
  commentId: number = 0;
  content: string = '';
  noteId: number | null = null;
  userId: number | null = null;
  username: string = '';
  avatar: string = '';
  createAt: string = '';
  parentCommentId: number | null = null;
  parentCommentUsername: string = '';
  replies: Array<CommentResponseDto> = [];
}