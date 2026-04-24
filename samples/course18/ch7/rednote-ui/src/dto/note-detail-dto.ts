export class NoteDetailDto {
  noteId: number = 0;
  title: string = '';
  content: string = '';
  images: Array<string> = [];
  topics: Array<string> = [];
  category: string = '';
  username: string = '';
  userId: number = 0;
  avatar: string = '';
  liked: boolean = false;
  likeCount: number = 0;
}