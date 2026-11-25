"""The saves that the desktop rules give a game."""

def save_substitutions(file_save, save_component):
    """What a generated main has for its saves.

    Without either, it has none: a game that needs a SaveComponent does not
    compile, instead of losing what it saves.

    Args:
      file_save: the file to save in, if any.
      save_component: the Scala type of another SaveComponent, if any.

    Returns:
      The substitutions of the template.
    """
    if file_save and save_component:
        fail("file_save and save_component are both set: choose how the game saves")
    mixin = ""
    member = ""
    if file_save:
        mixin = "\n  with _root_.sgl.desktop.FileSaveComponent"
        member = "override protected def saveFile: String = " + json.encode(file_save)
    elif save_component:
        # The ones of SGL can be given without their package.
        name = save_component if "." in save_component else "sgl." + save_component
        mixin = "\n  with " + (name if name.startswith("_root_.") else "_root_." + name)
    return {
        "{{SAVE_MIXIN}}": mixin,
        "{{SAVE_MEMBER}}": member,
    }
