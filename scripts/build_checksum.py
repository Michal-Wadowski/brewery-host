import sys
import zlib
from os import listdir, rename
from os.path import isfile, join

if len(sys.argv) < 2:
    raise Exception('No target name specified')

target = sys.argv[1]

curr_dir = './target'
target_files = [
    f for f
    in listdir(curr_dir)
    if isfile(join(curr_dir, f)) and f.startswith(target) and f.endswith('.jar')
]

for filename in target_files:
    with open(join(curr_dir, filename), 'rb') as f:
        content = f.read()

    filename_core = filename.split('.jar')[0]

    checksum = hex(zlib.crc32(content) & 0xffffffff)[2:]

    dst_filename = filename_core + '-' + checksum + '.jar'
    rename(join(curr_dir, filename), join(curr_dir, dst_filename))