# DECB File System

## FILE ALLOCATION TABLE

The file allocation table (FAT) is used to keep track of whether a granule has been allocated
to a file or if it is free. The FAT is composed of six control bytes followed by 68 data bytes -
one byte for each granule. The FAT is stored on sector two of the directory track (17).
A RAM image of the FAT is kept in the disk RAM for each of the four possible drives.
Keeping an image of the FAT in RAM helps speed up the overall operation of the DOS by eliminating
the need for disk I/O every time the DOS modifies the FAT. Saving the FAT to disk is done
approximately every 19 times that a new granule is pulled from the free granule reserve.
It is written to disk whenever a file is closed and there are some DOS operations, which force
the FAT to be written to disk when that DOS operation allocates a free granule.

Only the DOS uses two of the six control bytes. The first FAT control byte keeps track of how many
FCBs are active on the drive for a particular FAT. This byte is used to preclude the loading in of the
FAT from disk when there is any active file currently using the FAT. You can imagine the disaster,
which would occur if you were creating a file and had allocated some granules to your new file
but had not saved the new FAT to disk when the old FAT was loaded into RAM on top of the new FAT.
Your new file would be hopelessly gone. For that reason the DOS must not allow the FAT to be
loaded into RAM from disk while an FCB is active for that FAT.

The second FAT control byte is used to govern the need to write data from the FAT RAM image to the disk.
If the value of this byte is zero it means that the FAT RAM image is an exact copy of what is
currently stored on the disk. If the value is non-zero, it indicates that the data in the FAT RAM image
has been changed since the last time that the FAT was written to disk. The number stored in this byte is
an indicator of how many granules have been removed from the FAT since the last FAT to disk write.
Some BASIC commands, such as KILL, cause an immediate FAT RAM image to disk write when granules are either
freed or allocated. Other commands, which allocate granules, increment the second FAT control byte. This byte is
then compared to the disk variable WFATVL and when the second control byte >= WFATVL, the FAT is
written to disk.

The FAT data bytes are used to determine whether a granule is free and if it has been allocated they are
used to determine to which file the granule belongs. If a data byte is $FF, it means that the granule is free
and may be allocated to any file. If a granule has been allocated, it is part of a sector chain, which defines
which granules belong to a certain file. The only information required to be able to trace the granule chain is
the number of the first granule in the chain. If the first granule of the chain is not known, the chain cannot
be traced down backwards.

A granule data byte, which has been allocated, will contain a value, which is the number of the next granule
in the granule chain for that file. If the two most significant bits (6,7) of a granule data byte are set, then
that granule is the last granule in a file's granule chain. The low order four bits will contain the
number of sectors in the last granule, which the file uses. Even though a file may not use all the sectors
in the last granule in the chain, no other file may use the sectors. Disk space is not allocated on a sector basis,
it is allocated on a granule basis and the granule may not be broken down. The smallest one-byte file
will still require a full granule to be allocated in order to store the file.
