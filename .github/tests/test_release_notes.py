"""Check changelog extraction and preflight failures offline."""

from pathlib import Path
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]
PARSER = ROOT / '.github/scripts/parse_changelog.sh'


class ChangelogTest(unittest.TestCase):
    def test_extracts_real_14_entry_with_default_path_and_optional_v(self):
        for version in ('14.0.0', 'v14.0.0'):
            with self.subTest(version=version):
                result = subprocess.run(['bash', str(PARSER), version], cwd=ROOT, capture_output=True, text=True)
                self.assertEqual(0, result.returncode, result.stderr)
                self.assertTrue(result.stdout.startswith('# First public multiplatform release\n'))
                self.assertIn('## Known limitations\n', result.stdout)
                self.assertIn('Full changelog: https://github.com/MetrolistGroup/Metrolist/commits/v14.0.0\n', result.stdout)
                self.assertNotIn('---v', result.stdout)
                self.assertNotIn('KMP status update', result.stdout)

    def test_exact_version_and_entry_boundaries(self):
        with tempfile.TemporaryDirectory() as directory:
            changelog = Path(directory) / 'changelog.md'
            changelog.write_text('---v14x0x0\nWrong version\n---v14.0.0\nMastodon\n\n---v13.7.0\nTODO\n')
            result = subprocess.run(['bash', str(PARSER), '14.0.0', str(changelog)], capture_output=True, text=True)
            self.assertEqual(0, result.returncode, result.stderr)
            self.assertEqual('Mastodon\n', result.stdout)
            changelog.write_text('---v14x0x0\nWrong version\n')
            result = subprocess.run(['bash', str(PARSER), '14.0.0', str(changelog)], capture_output=True, text=True)
            self.assertNotEqual(0, result.returncode)
            self.assertIn('not found', result.stderr)
            self.assertEqual('', result.stdout)

    def test_rejects_empty_entries_and_todo_markers_without_output(self):
        for entry, error in (
            ('\n \t\n', 'empty changelog entry'),
            ('TODO: finish\n', 'TODO marker'),
            ('# Notes\n<!-- todo -->\n', 'TODO marker'),
            ('- Todo_fix\n', 'TODO marker'),
        ):
            with self.subTest(entry=entry), tempfile.TemporaryDirectory() as directory:
                changelog = Path(directory) / 'changelog.md'
                changelog.write_text(f'---v14.0.0\n{entry}---v13.7.0\nOld notes\n')
                result = subprocess.run(['bash', str(PARSER), '14.0.0', str(changelog)], capture_output=True, text=True)
                self.assertNotEqual(0, result.returncode)
                self.assertIn(error, result.stderr)
                self.assertEqual('', result.stdout)

    def test_missing_arguments_and_file(self):
        for arguments, error in (([], 'version argument required'), (['14.0.0', 'missing.md'], 'file not found')):
            with self.subTest(arguments=arguments), tempfile.TemporaryDirectory() as directory:
                result = subprocess.run(['bash', str(PARSER), *arguments], cwd=directory, capture_output=True, text=True)
                self.assertNotEqual(0, result.returncode)
                self.assertIn(error, result.stderr)


if __name__ == '__main__':
    unittest.main()
