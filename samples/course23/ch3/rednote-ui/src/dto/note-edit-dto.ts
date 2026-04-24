export class NoteEditDto {
  noteId: number = 0;
  title: string = '';
  content: string = '';
  images: Array<string> = [];
  topics: string = '';
  category: string = '';
}