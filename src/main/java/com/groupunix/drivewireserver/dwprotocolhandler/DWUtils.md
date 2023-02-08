# Drivewire Utils #

## Binary XDir File Descriptor ##

| Byte offset | Length | Description         | Details                                                |
|-------------|--------|---------------------|--------------------------------------------------------|
| 0           | 4      | File length (bytes) | Length of file. max = 4,294,967,295                    |
| 4           | 5      | Date and time       | OS9 formatted date and time record. 1 byte per element |
|             |        |                     | Bytes in order: YY MM DD HH mm                         |
| 9           | 1      | Is Directory        | 1 = Directory 0 = file                                 |
| 10          | 1      | Write Protected     | 1 = Read Only 0 = Read/Write                           |
| 11          | 1-255  | Filename            |                                                        |

