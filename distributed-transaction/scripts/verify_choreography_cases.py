#!/usr/bin/env -S uv run --script
# /// script
# requires-python = ">=3.11"
# dependencies = [
#   "httpx>=0.28.1",
# ]
# ///

from choreography_verifier.main import main


if __name__ == "__main__":
    main()
