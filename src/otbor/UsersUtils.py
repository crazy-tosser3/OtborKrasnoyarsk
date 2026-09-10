from argon2 import PasswordHasher
from argon2.exceptions import VerifyMismatchError
from sqlalchemy import create_engine, select
from sqlalchemy.orm import Session

from src.otbor.database.models import Base, User

ph = PasswordHasher()


class UserError(Exception):
    pass


class UserNotFound(UserError):
    pass


class UserAlreadyExists(UserError):
    pass


class InvalidCredentials(UserError):
    pass


# ---------- Класс для работы с пользователями ----------


class Users:
    def __init__(self, engine):
        """
        :param engine: SQLAlchemy engine (create_engine(...))
        """
        self.engine = engine
        Base.metadata.create_all(self.engine)

    @staticmethod
    def __hash_password(password: str) -> str:
        return ph.hash(password)

    @staticmethod
    def __verify_password(password_hash: str, password: str) -> bool:
        try:
            return ph.verify(password_hash, password)
        except VerifyMismatchError:
            return False

    def create_user(self, username: str, password: str) -> User:
        """МЕТОД ДЛЯ СОЗДАНИЯ ПОЛЬЗОВАТЕЛЯ"""
        with Session(self.engine) as session:
            existing = session.scalar(select(User).where(User.username == username))
            if existing is not None:
                raise UserAlreadyExists(f"Пользователь '{username}' уже существует")

            user = User(
                username=username,
                password_hash=self.__hash_password(password),
            )
            session.add(user)
            session.commit()
            session.refresh(user)
            return user

    def _authenticate(self, session: Session, username: str, password: str) -> User:
        """Внутренний метод: находит пользователя и проверяет пароль."""
        user = session.scalar(select(User).where(User.username == username))
        if user is None:
            raise UserNotFound(f"Пользователь '{username}' не найден")

        if not self.__verify_password(user.password_hash, password):
            raise InvalidCredentials("Неверный пароль")

        return user

    def delete_user(self, username: str, password: str) -> None:
        """МЕТОД ДЛЯ УДАЛЕНИЯ ПОЛЬЗОВАТЕЛЯ"""
        with Session(self.engine) as session:
            user = self._authenticate(session, username, password)
            session.delete(user)
            session.commit()

    def update_user(
        self,
        username: str,
        password: str,
        new_username: str | None = None,
        new_password: str | None = None,
    ) -> User:
        """МЕТОД ДЛЯ ОБНОВЛЕНИЯ ДАННЫХ О ПОЛЬЗОВАТЕЛЕ"""
        with Session(self.engine) as session:
            user = self._authenticate(session, username, password)

            if new_username is not None:
                existing = session.scalar(
                    select(User).where(User.username == new_username)
                )
                if existing is not None and existing.id != user.id:
                    raise UserAlreadyExists(f"Имя '{new_username}' уже занято")
                user.username = new_username

            if new_password is not None:
                user.password_hash = self.__hash_password(new_password)

            session.commit()
            session.refresh(user)
            return user

    def get_user(self, username: str) -> User:
        with Session(self.engine) as session:
            user = session.scalar(select(User).where(User.username == username))
            if user is None:
                raise UserNotFound(f"Пользователь '{username}' не найден")
            return user
