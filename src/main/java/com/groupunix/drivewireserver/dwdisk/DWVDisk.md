ftp://davidgunn.org/coco/VARIOUS/INFO/MISC/VDK_Format.txt


Dragon Emulator Virtual Disk (VDK) Format  
Paul Burgin / v1.0 / April 1999

The new virtual disk format used by PC-Dragon v2.05 has at least 12 header
bytes as follows:

| Offset | Size | Item                        | Notes                                                                                                                                                                                                                                                                                                                                                          |
|--------|------|:----------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 0      | 2	| Signature ('dk')            | MSB first. Note lower case to differentiate a VDK from a DOS ROM file.                                                                                                                                                                                                                                                                                         |
| 2	  | 2	| Header length               | LSB first. Total length of the header (equal to the offset to the start of disk data). Intended to allow the header to be extended easily.                                                                                                                                                                                                                     |
| 4	  | 1	| VDK version - actual        | Indicates the version of the VDK format used to write the file. Currently $10 (VDK v1.0).                                                                                                                                                                                                                                                                      |
| 5	  | 1 | VDK version - compatibility | For backwards compatibility, this indicates the minimum version of the VDK format that can be used to read the file. Effectively it says to the emulator 'If you can understand VDK version X then you can understand this file. Usually this byte will be the same as the previous one, but if a minor addition is made to the header then it becomes useful. |
| 6      | 1 | Source id                   | Indicates how the file was created. Essentially this is for information only, but may be useful for debugging. The following values are defined:                                                                                                                                                                                                               |
|        | |                             | 0 = created by hand                                                                                                                                                                                                                                                                                                                                            |
|        | |                             | 1 = header stub                                                                                                                                                                                                                                                                                                                                                |
|        | |                             | 2 = mkdsk.exe                                                                                                                                                                                                                                                                                                                                                  |
|        | |                             | 3 = other tools                                                                                                                                                                                                                                                                                                                                                |
|        | |                             | 'P' = PC-Dragon                                                                                                                                                                                                                                                                                                                                                |
|        | |                             | 'T' = T3                                                                                                                                                                                                                                                                                                                                                       |
|        | |                             | \>$7F = other emulator                                                                                                                                                                                                                                                                                                                                         |
| 7	  | 1 | Source version              | Version information for the source identified above. E.g. $25=v2.05                                                                                                                                                                                                                                                                                            |
| 8      | 1 | Number of tracks            | 40 or 80                                                                                                                                                                                                                                                                                                                                                       |
| 9      | 1 | Number of sides             | 1 or 2                                                                                                                                                                                                                                                                                                                                                         |
| 10     | 1 | Flags                       | bit 0 = write protect [0=off, 1=on]                                                                                                                                                                                                                                                                                                                            |
|        | |                             | bit 1 = lock (advisory) [0=off, 1=on]                                                                                                                                                                                                                                                                                                                          |
|        | |                             | bit 2 = lock (mandatory) [0=off, 1=on]                                                                                                                                                                                                                                                                                                                         |
|        | |                             | bit 3 = disk-set [0=last disk, 1=not last disk]                                                                                                                                                                                                                                                                                                                |
|        | |                             | bits 4-7 = unused in VDK v1.0                                                                                                                                                                                                                                                                                                                                  |
| 11     | 1 | Compression & Name length   | bits 0-2 = compression [0=off, >0=TBD]                                                                                                                                                                                                                                                                                                                         |
|        | |                             | bits 3-7 = disk name length [min 0, max 31]                                                                                                                                                                                                                                                                                                                    |
| 12     | 0-31 | Disk name                   | Optional ASCII name for the virtual disk. Not zero terminated. (min 12) 0+ Compression variables TBD                                                                                                                                                                                                                                                           |

Some of the above needs a little more explanation. The write-protect
ability is included as a bit in the header so that it can survive
circumstances which file attributes might not, if necessary (e.g. public
file servers, e-mail attachments). This also allows easy change from
within an emulator, and might be useful for disk-sets (see below).
Support for write protection by file attributes is at the option of each
individual emulator.

The lock bits were added as a possible approach for preventing a disk
being loaded more than once. It is typically not required for a single
instance of an emulator, but may be used across multiple simultaneous
emulators, multiple instances of the same emulator, or in multi-user
environments. The difference between bit1 and bit2 is that the user is
asked whether they wish to ignore the bit1 lock but are not
allowed to override the bit2 lock. A well-behaved emulator should at
least obey the locks upon opening the VDK, but for the current v1.0 of
the file format need not set the locks unless it wishes to. PC-Dragon
v2.05 does not set either of the locks (I felt a little uncomfortable
about modifying the disk without the user's consent) but they may be
used in the future.

The disk-set bit allows more than one virtual disk to be stored in a
single VDK file. This is intended for software supplied on >1 disk, or
for a collection of similar disks. Emulators may allow disk-sets to be
created/modified/loaded at their option. PC-Dragon v2.05 supports
loading of disk-sets, but the user interface is rather basic and there's
no facility for creating disk-sets.

Virtual disk compression has been anticipated by the format, but is left
TBD for the moment due to the complexity of randomly accessing a
compressed file. The disk name is optional and isn't ever displayed by
PC-Dragon v2.05. Any data associated with compression is assumed to
follow the disk name.
